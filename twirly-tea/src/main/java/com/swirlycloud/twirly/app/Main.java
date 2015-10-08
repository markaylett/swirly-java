/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.io.CacheUtil.NO_CACHE;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jdt.annotation.NonNull;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.ServFactory;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.mock.MockDatastore;
import com.swirlycloud.twirly.quickfix.Slf4jLogFactory;

public final class Main {

    private static final @NonNull Factory FACTORY = new ServFactory();

    public static Properties readProperties(String path) throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path)) {
            final Properties props = new Properties();
            props.load(is);
            return props;
        }
    }

    public static SessionSettings readSettings(String path) throws ConfigError, IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path)) {
            return new SessionSettings(is);
        }
    }

    public static FixServ startFixServ(String path, LockableServ serv)
            throws ConfigError, FieldConvertError, NotFoundException, IOException {
        final SessionSettings settings = readSettings(path);
        final LogFactory logFactory = new Slf4jLogFactory();
        return FixServ.create(settings, serv, logFactory);
    }

    public static FixClnt startFixClient(String path)
            throws ConfigError, FieldConvertError, IOException {
        final SessionSettings settings = readSettings(path);
        final LogFactory logFactory = new Slf4jLogFactory();
        return FixClnt.create(settings, logFactory);
    }

    public static void run(final LockableServ serv) throws Exception {
        final String mnem = "EURUSD.MAR14";
        final String display = "EURUSD March 14";
        final String contr = "EURUSD";
        final int today = ymdToJd(2014, 2, 11);
        final int settlDay = today + 2;
        final int expiryDay = today + 1;
        final int state = 0x01;
        final long now = jdToMillis(today);

        serv.acquireWrite();
        try {
            serv.createMarket(mnem, display, contr, settlDay, expiryDay, state, now);
        } finally {
            serv.releaseWrite();
        }

        try (final FixServ fixServ = startFixServ("FixServ.conf", serv)) {

            try (final FixClnt fixClient = startFixClient("FixClnt.conf")) {

                fixClient.waitForLogon();

                final SessionID marayl = new SessionID("FIX.4.4", "MarkAylett", "Twirly");
                final SessionID gosayl = new SessionID("FIX.4.4", "GoskaAylett", "Twirly");

                fixClient.sendNewOrderSingle("EURUSD.MAR14", "marayl1", Side.BUY, 12345, 10, 1, now,
                        marayl).get();
                fixClient.sendNewOrderSingle("EURUSD.MAR14", "gosayl1", Side.SELL, 12345, 5, 1, now,
                        gosayl).get();
                fixClient.sendOrderCancelReplaceRequest("EURUSD.MAR14", "marayl2", "marayl1", 9,
                        now, marayl).get();
                fixClient.sendOrderCancelRequest("EURUSD.MAR14", "marayl3", "marayl1", now, marayl)
                        .get();
                fixClient.sendOrderCancelRequest("EURUSD.MAR14", "marayl3", "marayl1", now, marayl)
                        .get();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure(readProperties("log4j.properties"));
        try (final Datastore datastore = new MockDatastore()) {
            final LockableServ serv = new LockableServ(datastore, NO_CACHE, FACTORY, now());
            run(serv);
        }
    }
}

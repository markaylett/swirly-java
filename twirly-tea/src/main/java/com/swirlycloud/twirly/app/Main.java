/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.fix.FixUtility.readProperties;
import static com.swirlycloud.twirly.io.CacheUtil.NO_CACHE;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.ServFactory;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.fix.FixClnt;
import com.swirlycloud.twirly.fix.FixServ;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.mock.MockDatastore;
import com.swirlycloud.twirly.quickfix.Slf4jLogFactory;

import quickfix.LogFactory;
import quickfix.SessionID;

public final class Main {

    private static final @NonNull Factory FACTORY = new ServFactory();

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

        final LogFactory logFactory = new Slf4jLogFactory();
        try (final FixServ fixServ = FixServ.create("FixServ.conf", serv, logFactory)) {

            try (final FixClnt fixClient = FixClnt.create("FixClnt.conf", logFactory)) {

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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.SocketInitiator;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.mock.MockModel;
import com.swirlycloud.twirly.quickfix.NullStoreFactory;
import com.swirlycloud.twirly.quickfix.Slf4jLogFactory;

public final class Main {

    public static AutoCloseable newFixAcceptor(final Application application,
            final SessionSettings settings, final LogFactory logFactory) throws ConfigError {
        final Acceptor acceptor = new SocketAcceptor(application, new NullStoreFactory(), settings,
                logFactory, new DefaultMessageFactory());
        try {
            acceptor.start();
            return new AutoCloseable() {
                @Override
                public final void close() throws Exception {
                    acceptor.stop();
                }
            };
        } catch (ConfigError e) {
            // If a ConfigError is thrown during start(), QuickFIX may leak a daemonised thread,
            // which prevents a fail-fast shutdown, because the thread is blocked in a poll/select
            // call. The API does not offer any facility to forcibly disconnect or interrupt this
            // thread, so we use System.exit() as a last resort.
            e.printStackTrace();
            System.exit(1);
            throw e;
        }
    }

    public static AutoCloseable newFixInitiator(final Application application,
            final SessionSettings settings, final LogFactory logFactory) throws ConfigError {
        final Initiator initiator = new SocketInitiator(application, new NullStoreFactory(),
                settings, logFactory, new DefaultMessageFactory());
        try {
            initiator.start();
            return new AutoCloseable() {
                @Override
                public final void close() throws Exception {
                    initiator.stop();
                }
            };
        } catch (ConfigError e) {
            // See comment in newAcceptor().
            e.printStackTrace();
            System.exit(1);
            throw e;
        }
    }

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

    public static AutoCloseable startFixServ(String path, LockableServ serv) throws ConfigError,
            FieldConvertError, NotFoundException, IOException {
        final SessionSettings settings = readSettings(path);
        final Application application = new FixServer(settings, serv);
        final LogFactory logFactory = new Slf4jLogFactory();
        return newFixAcceptor(application, settings, logFactory);
    }

    public static AutoCloseable startFixClient(String path) throws ConfigError, FieldConvertError,
            IOException {
        final SessionSettings settings = readSettings(path);
        final Application application = new FixClient(settings);
        final LogFactory logFactory = new Slf4jLogFactory();
        return newFixInitiator(application, settings, logFactory);
    }

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure(readProperties("log4j.properties"));
        try (final Model model = new MockModel()) {

            final LockableServ serv = new LockableServ(model, now());
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

            try (final AutoCloseable fixServ = startFixServ("FixServ.conf", serv)) {

                Thread.sleep(2000);

                try (final AutoCloseable fixClient = startFixClient("FixClient.conf")) {

                    Thread.sleep(2000);

                    final SessionID marayl = new SessionID("FIX.4.4", "MarkAylett", "Twirly");
                    final SessionID gosayl = new SessionID("FIX.4.4", "GoskaAylett", "Twirly");

                    final FixBuilder builder = new FixBuilder();
                    builder.setMessage(new NewOrderSingle());
                    builder.setNewOrderSingle("EURUSD.MAR14", "marayl1", Action.BUY, 12345, 10, 1,
                            now);
                    Session.sendToTarget(builder.getMessage(), marayl);

                    Thread.sleep(2000);

                    builder.setMessage(new NewOrderSingle());
                    builder.setNewOrderSingle("EURUSD.MAR14", "gosayl1", Action.SELL, 12345, 5, 1,
                            now);
                    Session.sendToTarget(builder.getMessage(), gosayl);

                    Thread.sleep(2000);

                    builder.setMessage(new OrderCancelReplaceRequest());
                    builder.setOrderCancelReplaceRequest("EURUSD.MAR14", "marayl2", "marayl1", 9,
                            now);
                    Session.sendToTarget(builder.getMessage(), marayl);

                    Thread.sleep(2000);

                    builder.setMessage(new OrderCancelRequest());
                    builder.setOrderCancelRequest("EURUSD.MAR14", "marayl3", "marayl1", now);
                    Session.sendToTarget(builder.getMessage(), marayl);

                    Thread.sleep(2000);
                }
            }
        }
    }
}

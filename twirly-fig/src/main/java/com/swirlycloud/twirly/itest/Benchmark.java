/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.itest;

import static com.swirlycloud.twirly.io.CacheUtil.NO_CACHE;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.app.Serv;
import com.swirlycloud.twirly.app.Result;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.MarketBook;
import com.swirlycloud.twirly.domain.ServFactory;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.TraderSess;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.mock.MockDatastore;

// -server -verbose:gc -Xprof

public final class Benchmark {

    private static final @NonNull Factory FACTORY = new ServFactory();

    private static void run(final Serv serv) throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final TraderSess marayl = serv.getTrader("MARAYL");
        final TraderSess gosayl = serv.getTrader("GOSAYL");
        final TraderSess tobayl = serv.getTrader("TOBAYL");
        final TraderSess emiayl = serv.getTrader("EMIAYL");

        final MarketBook book = serv.createMarket("EURUSD", "EURUSD", "EURUSD", 0, 0, 0, now());
        assert book != null;

        try (final Result result = new Result()) {
            for (int i = 0; i < 250000; ++i) {
                final long now = now();
                final long startNanos = System.nanoTime();

                // Maker sell-side.
                serv.createOrder(marayl, book, "", Side.SELL, 12348, 5, 1, now, result);
                serv.createOrder(gosayl, book, "", Side.SELL, 12348, 5, 1, now, result);
                serv.createOrder(marayl, book, "", Side.SELL, 12348, 5, 1, now, result);
                serv.createOrder(gosayl, book, "", Side.SELL, 12347, 5, 1, now, result);
                serv.createOrder(marayl, book, "", Side.SELL, 12347, 5, 1, now, result);
                serv.createOrder(gosayl, book, "", Side.SELL, 12346, 5, 1, now, result);

                // Maker buy-side.
                serv.createOrder(marayl, book, "", Side.BUY, 12344, 5, 1, now, result);
                serv.createOrder(gosayl, book, "", Side.BUY, 12343, 5, 1, now, result);
                serv.createOrder(marayl, book, "", Side.BUY, 12343, 5, 1, now, result);
                serv.createOrder(gosayl, book, "", Side.BUY, 12342, 5, 1, now, result);
                serv.createOrder(marayl, book, "", Side.BUY, 12342, 5, 1, now, result);
                serv.createOrder(gosayl, book, "", Side.BUY, 12342, 5, 1, now, result);

                // Taker sell-side.
                serv.createOrder(tobayl, book, "", Side.SELL, 12342, 30, 1, now, result);

                // Taker buy-side.
                serv.createOrder(emiayl, book, "", Side.BUY, 12348, 30, 1, now, result);

                serv.archiveOrder(marayl, now);
                serv.archiveTrade(marayl, now);
                serv.archiveOrder(gosayl, now);
                serv.archiveTrade(gosayl, now);
                serv.archiveOrder(tobayl, now);
                serv.archiveTrade(tobayl, now);
                serv.archiveOrder(emiayl, now);
                serv.archiveTrade(emiayl, now);

                final long totalNanos = System.nanoTime() - startNanos;
                if ((i % 1000) == 0) {
                    System.out.println(totalNanos / 1000L + " usec");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try (final Datastore datastore = new MockDatastore()) {
            final Serv serv = new Serv(datastore, NO_CACHE, FACTORY, now());
            run(serv);
        }
    }
}

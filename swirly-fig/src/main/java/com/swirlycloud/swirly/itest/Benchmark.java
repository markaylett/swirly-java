/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.itest;

import static com.swirlycloud.swirly.io.CacheUtil.NO_CACHE;
import static com.swirlycloud.swirly.util.TimeUtil.getTimeOfDay;

import java.io.IOException;

import com.swirlycloud.swirly.app.Response;
import com.swirlycloud.swirly.app.Serv;
import com.swirlycloud.swirly.book.MarketBook;
import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.entity.TraderSess;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.io.Datastore;
import com.swirlycloud.swirly.mock.MockDatastore;

// -server -verbose:gc -Xprof

public final class Benchmark {

    private static void run(final Serv serv) throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final TraderSess marayl = serv.getTrader("MARAYL");
        final TraderSess gosayl = serv.getTrader("GOSAYL");
        final TraderSess tobayl = serv.getTrader("TOBAYL");
        final TraderSess emiayl = serv.getTrader("EMIAYL");

        final MarketBook book = serv.createMarket("EURUSD", "EURUSD", "EURUSD", 0, 0, 0, getTimeOfDay());
        assert book != null;

        try (final Response resp = new Response()) {
            for (int i = 0; i < 250000; ++i) {
                final long now = getTimeOfDay();
                final long startNanos = System.nanoTime();

                // Maker sell-side.
                serv.createOrder(marayl, book, "", 0, Side.SELL, 5, 12348, 1, now, resp);
                serv.createOrder(gosayl, book, "", 0, Side.SELL, 5, 12348, 1, now, resp);
                serv.createOrder(marayl, book, "", 0, Side.SELL, 5, 12348, 1, now, resp);
                serv.createOrder(gosayl, book, "", 0, Side.SELL, 5, 12347, 1, now, resp);
                serv.createOrder(marayl, book, "", 0, Side.SELL, 5, 12347, 1, now, resp);
                serv.createOrder(gosayl, book, "", 0, Side.SELL, 5, 12346, 1, now, resp);

                // Maker buy-side.
                serv.createOrder(marayl, book, "", 0, Side.BUY, 5, 12344, 1, now, resp);
                serv.createOrder(gosayl, book, "", 0, Side.BUY, 5, 12343, 1, now, resp);
                serv.createOrder(marayl, book, "", 0, Side.BUY, 5, 12343, 1, now, resp);
                serv.createOrder(gosayl, book, "", 0, Side.BUY, 5, 12342, 1, now, resp);
                serv.createOrder(marayl, book, "", 0, Side.BUY, 5, 12342, 1, now, resp);
                serv.createOrder(gosayl, book, "", 0, Side.BUY, 5, 12342, 1, now, resp);

                // Taker sell-side.
                serv.createOrder(tobayl, book, "", 0, Side.SELL, 30, 12342, 1, now, resp);

                // Taker buy-side.
                serv.createOrder(emiayl, book, "", 0, Side.BUY, 30, 12348, 1, now, resp);

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
            final Serv serv = new Serv(datastore, datastore, NO_CACHE, getTimeOfDay());
            run(serv);
        }
    }
}

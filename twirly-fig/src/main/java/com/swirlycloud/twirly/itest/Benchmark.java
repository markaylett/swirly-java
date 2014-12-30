/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.itest;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;

import java.io.IOException;

import com.swirlycloud.twirly.app.Accnt;
import com.swirlycloud.twirly.app.Serv;
import com.swirlycloud.twirly.app.Trans;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.mock.MockModel;

// -server -verbose:gc -Xprof

public final class Benchmark {

    private static void run(final Serv s) throws BadRequestException, NotFoundException,
            IOException {
        final Accnt marayl = s.getLazyAccnt("MARAYL");
        final Accnt gosayl = s.getLazyAccnt("GOSAYL");
        final Accnt tobayl = s.getLazyAccnt("TOBAYL");
        final Accnt emiayl = s.getLazyAccnt("EMIAYL");

        final int settlDay = ymdToJd(2014, 3, 14);
        final int expiryDay = ymdToJd(2014, 3, 12);
        final Market market = s.createMarket("EURUSD", settlDay, expiryDay,
                System.currentTimeMillis());
        assert market != null;

        final Trans trans = new Trans();

        for (int i = 0; i < 250000; ++i) {
            final long now = System.currentTimeMillis();
            final long startNanos = System.nanoTime();

            // Maker sell-side.
            s.placeOrder(marayl, market, "", Action.SELL, 12348, 5, 1, now, trans);
            s.placeOrder(gosayl, market, "", Action.SELL, 12348, 5, 1, now, trans);
            s.placeOrder(marayl, market, "", Action.SELL, 12348, 5, 1, now, trans);
            s.placeOrder(gosayl, market, "", Action.SELL, 12347, 5, 1, now, trans);
            s.placeOrder(marayl, market, "", Action.SELL, 12347, 5, 1, now, trans);
            s.placeOrder(gosayl, market, "", Action.SELL, 12346, 5, 1, now, trans);

            // Maker buy-side.
            s.placeOrder(marayl, market, "", Action.BUY, 12344, 5, 1, now, trans);
            s.placeOrder(gosayl, market, "", Action.BUY, 12343, 5, 1, now, trans);
            s.placeOrder(marayl, market, "", Action.BUY, 12343, 5, 1, now, trans);
            s.placeOrder(gosayl, market, "", Action.BUY, 12342, 5, 1, now, trans);
            s.placeOrder(marayl, market, "", Action.BUY, 12342, 5, 1, now, trans);
            s.placeOrder(gosayl, market, "", Action.BUY, 12342, 5, 1, now, trans);

            // Taker sell-side.
            s.placeOrder(tobayl, market, "", Action.SELL, 12342, 30, 1, now, trans);

            // Taker buy-side.
            s.placeOrder(emiayl, market, "", Action.BUY, 12348, 30, 1, now, trans);

            s.archiveAll(marayl, now);
            s.archiveAll(gosayl, now);
            s.archiveAll(tobayl, now);
            s.archiveAll(emiayl, now);

            long totalNanos = System.nanoTime() - startNanos;
            if ((i % 1000) == 0) {
                System.out.println(totalNanos / 1000L + " usec");
            }
        }
    }

    public static void main(String[] args) throws BadRequestException, NotFoundException,
            IOException {
        try (final Serv s = new Serv(new MockModel())) {
            run(s);
        }
    }
}

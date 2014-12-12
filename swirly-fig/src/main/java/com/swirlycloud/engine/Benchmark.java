/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static com.swirlycloud.util.Date.ymdToJd;

import java.io.IOException;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Market;
import com.swirlycloud.exception.BadRequestException;
import com.swirlycloud.exception.NotFoundException;
import com.swirlycloud.mock.MockModel;

// -server -verbose:gc -Xprof

public final class Benchmark {

    private static void run(final Serv s) throws BadRequestException, NotFoundException,
            IOException {
        final Accnt marayl = s.getLazyAccnt("MARAYL");
        final Accnt gosayl = s.getLazyAccnt("GOSAYL");
        final Accnt tobayl = s.getLazyAccnt("TOBAYL");
        final Accnt emiayl = s.getLazyAccnt("EMIAYL");

        final int settlDay = ymdToJd(2014, 3, 14);
        final Market market = s.getLazyMarket("EURUSD", settlDay);
        assert market != null;

        final Trans trans = new Trans();

        for (int i = 0; i < 250000; ++i) {
            final long startNanos = System.nanoTime();

            // Maker sell-side.
            s.placeOrder(marayl, market, "", Action.SELL, 12348, 5, 1, trans);
            s.placeOrder(gosayl, market, "", Action.SELL, 12348, 5, 1, trans);
            s.placeOrder(marayl, market, "", Action.SELL, 12348, 5, 1, trans);
            s.placeOrder(gosayl, market, "", Action.SELL, 12347, 5, 1, trans);
            s.placeOrder(marayl, market, "", Action.SELL, 12347, 5, 1, trans);
            s.placeOrder(gosayl, market, "", Action.SELL, 12346, 5, 1, trans);

            // Maker buy-side.
            s.placeOrder(marayl, market, "", Action.BUY, 12344, 5, 1, trans);
            s.placeOrder(gosayl, market, "", Action.BUY, 12343, 5, 1, trans);
            s.placeOrder(marayl, market, "", Action.BUY, 12343, 5, 1, trans);
            s.placeOrder(gosayl, market, "", Action.BUY, 12342, 5, 1, trans);
            s.placeOrder(marayl, market, "", Action.BUY, 12342, 5, 1, trans);
            s.placeOrder(gosayl, market, "", Action.BUY, 12342, 5, 1, trans);

            // Taker sell-side.
            s.placeOrder(tobayl, market, "", Action.SELL, 12342, 30, 1, trans);

            // Taker buy-side.
            s.placeOrder(emiayl, market, "", Action.BUY, 12348, 30, 1, trans);

            s.archiveAll(marayl);
            s.archiveAll(gosayl);
            s.archiveAll(tobayl);
            s.archiveAll(emiayl);

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

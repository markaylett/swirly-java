/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.itest;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.app.Serv;
import com.swirlycloud.twirly.app.Sess;
import com.swirlycloud.twirly.app.Trans;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.BasicFactory;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.mock.MockDatastore;

// -server -verbose:gc -Xprof

public final class Benchmark {

    private static final @NonNull Factory FACTORY = new BasicFactory();

    private static void run(final Serv s) throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final Sess marayl = s.getLazySess("MARAYL");
        final Sess gosayl = s.getLazySess("GOSAYL");
        final Sess tobayl = s.getLazySess("TOBAYL");
        final Sess emiayl = s.getLazySess("EMIAYL");

        final int settlDay = ymdToJd(2014, 2, 14);
        final int expiryDay = ymdToJd(2014, 2, 12);
        final int state = 0x01;
        final Market market = s.createMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", settlDay,
                expiryDay, state, now());
        assert market != null;

        try (final Trans trans = new Trans()) {
            for (int i = 0; i < 250000; ++i) {
                final long now = now();
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
    }

    public static void main(String[] args) throws Exception {
        @SuppressWarnings("resource")
        final Datastore datastore = new MockDatastore(FACTORY);
        boolean success = false;
        try {
            try (final Serv serv = new Serv(datastore, FACTORY, now())) {
                success = true;
                run(serv);
            }
        } finally {
            if (!success) {
                datastore.close();
            }
        }
    }
}

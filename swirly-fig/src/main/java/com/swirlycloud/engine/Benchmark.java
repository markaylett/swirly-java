/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static com.swirlycloud.util.Date.ymdToJd;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.mock.MockModel;

public final class Benchmark {

    private static void run(final Serv s) {
        final Accnt accnt = s.getLazyAccnt("MARAYL");
        assert accnt != null;

        final int settlDay = ymdToJd(2014, 3, 14);
        final Market market = s.getLazyMarket("EURUSD", settlDay);
        assert market != null;

        final Trans trans = new Trans();
        final Order order = s.placeOrder(accnt, market, "", Action.BUY, 12345, 5, 1, trans)
                .getOrder();
        s.reviseOrder(accnt, market, order, 4, trans);
    }

    public static void main(String[] args) {
        try (final Serv s = new Serv(new MockModel())) {
            run(s);
        }
    }
}

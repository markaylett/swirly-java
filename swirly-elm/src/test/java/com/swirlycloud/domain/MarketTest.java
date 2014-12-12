/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Trader;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockTrader;

public final class MarketTest {
    @Test
    public final void test() throws IOException {
        final Contr contr = MockContr.newContr("EURUSD");
        final int settlDay = ymdToJd(2014, 3, 14);
        final Market market = new Market(contr, settlDay, settlDay);

        final Trader trader = MockTrader.newTrader("MARAYL");
        final long now = 1414932078620L;

        market.placeOrder(new Order(1, trader, contr, settlDay, "apple", Action.BUY, 12343, 10, 0,
                now), now);
        market.placeOrder(new Order(1, trader, contr, settlDay, "orange", Action.BUY, 12344, 5, 0,
                now), now);
        market.placeOrder(new Order(1, trader, contr, settlDay, "pear", Action.SELL, 12346, 5, 0,
                now), now);
        market.placeOrder(new Order(1, trader, contr, settlDay, "banana", Action.SELL, 12346, 2, 0,
                now), now);

        final StringBuilder sb = new StringBuilder();
        market.toJson(sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"expiryDate\":20140314,\"bidTicks\":[12344,12343,null],\"bidLots\":[5,10,null],\"bidCount\":[1,1,null],\"offerTicks\":[12346,null,null],\"offerLots\":[7,null,null],\"offerCount\":[2,null,null],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null}",
                sb.toString());
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static com.swirlycloud.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.User;
import com.swirlycloud.engine.Market;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockUser;

public final class MarketTest {
    @Test
    public final void test() {
        final Contr contr = MockContr.newContr("EURUSD");
        final int settlDay = ymdToJd(2014, 3, 14);
        final Market market = new Market(contr, settlDay);

        final User user = MockUser.newUser("MARAYL");
        final long now = 1414932078620L;

        market.placeOrder(new Order(1, user, contr, settlDay, "apple", Action.BUY, 12343, 10, 0,
                now), now);
        market.placeOrder(new Order(1, user, contr, settlDay, "orange", Action.BUY, 12344, 5, 0,
                now), now);
        market.placeOrder(
                new Order(1, user, contr, settlDay, "pear", Action.SELL, 12346, 5, 0, now), now);
        market.placeOrder(new Order(1, user, contr, settlDay, "banana", Action.SELL, 12346, 2, 0,
                now), now);

        final StringBuilder sb = new StringBuilder();

        // Default to TOB.
        market.print(sb, null);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"bidTicks\":12344,\"bidLots\":5,\"bidCount\":1,\"offerTicks\":12346,\"offerLots\":7,\"offerCount\":2}",
                sb.toString());

        // Explicit TOB.
        sb.setLength(0);
        market.print(sb, Integer.valueOf(1));
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"bidTicks\":12344,\"bidLots\":5,\"bidCount\":1,\"offerTicks\":12346,\"offerLots\":7,\"offerCount\":2}",
                sb.toString());

        // Round-up to minimum.
        sb.setLength(0);
        market.print(sb, Integer.valueOf(-1));
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"bidTicks\":12344,\"bidLots\":5,\"bidCount\":1,\"offerTicks\":12346,\"offerLots\":7,\"offerCount\":2}",
                sb.toString());

        // Somewhere between minimum and maximum.
        sb.setLength(0);
        market.print(sb, Integer.valueOf(3));
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"bidTicks\":[12344,12343,0],\"bidLots\":[5,10,0],\"bidCount\":[1,1,0],\"offerTicks\":[12346,0,0],\"offerLots\":[7,0,0],\"offerCount\":[2,0,0]}",
                sb.toString());

        // Round-down to maximum.
        sb.setLength(0);
        market.print(sb, Integer.valueOf(10));
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"bidTicks\":[12344,12343,0,0,0],\"bidLots\":[5,10,0,0,0],\"bidCount\":[1,1,0,0,0],\"offerTicks\":[12346,0,0,0,0],\"offerLots\":[7,0,0,0,0],\"offerCount\":[2,0,0,0,0]}",
                sb.toString());
    }
}

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
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.State;
import com.swirlycloud.engine.Accnt;
import com.swirlycloud.engine.Market;
import com.swirlycloud.engine.Serv;
import com.swirlycloud.engine.Trans;
import com.swirlycloud.mock.MockModel;

public final class ServTest {
    @Test
    public final void test() {
        try (final Serv s = new Serv(new MockModel())) {
            final Accnt accnt = s.getLazyAccnt("MARAYL");
            final int settlDay = ymdToJd(2014, 3, 14);
            final Market market = s.getLazyMarket("EURUSD", settlDay);

            final Trans trans = new Trans();
            final Order order = s.placeOrder(accnt, market, "", Action.BUY, 12345, 5, 1, trans)
                    .getOrder();
            assertEquals(accnt.getUser(), order.getUser());
            assertEquals(market.getContr(), order.getContr());
            assertEquals(settlDay, order.getSettlDay());
            assertEquals("", order.getRef());
            assertEquals(State.NEW, order.getState());
            assertEquals(Action.BUY, order.getAction());
            assertEquals(12345, order.getTicks());
            assertEquals(5, order.getLots());
            assertEquals(5, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getLastTicks());
            assertEquals(0, order.getLastLots());
            assertEquals(1, order.getMinLots());
            assertEquals(order.getCreated(), order.getModified());

            s.reviseOrder(accnt, order, 4, trans);
            assertEquals(accnt.getUser(), order.getUser());
            assertEquals(market.getContr(), order.getContr());
            assertEquals(settlDay, order.getSettlDay());
            assertEquals("", order.getRef());
            assertEquals(State.REVISE, order.getState());
            assertEquals(Action.BUY, order.getAction());
            assertEquals(12345, order.getTicks());
            assertEquals(4, order.getLots());
            assertEquals(4, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getLastTicks());
            assertEquals(0, order.getLastLots());
            assertEquals(1, order.getMinLots());
        }
    }
}

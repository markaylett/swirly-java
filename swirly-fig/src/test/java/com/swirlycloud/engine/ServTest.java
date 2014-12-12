/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static com.swirlycloud.date.DateUtil.ymdToJd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.State;
import com.swirlycloud.exception.BadRequestException;
import com.swirlycloud.exception.NotFoundException;
import com.swirlycloud.mock.MockModel;

public final class ServTest {
    @Test
    public final void test() throws BadRequestException, NotFoundException {
        try (final Serv s = new Serv(new MockModel())) {
            final Accnt accnt = s.getLazyAccnt("MARAYL");
            assertNotNull(accnt);

            final int settlDay = ymdToJd(2014, 3, 14);
            final int expiryDay = ymdToJd(2014, 3, 12);
            final Market market = s.createMarket("EURUSD", settlDay, expiryDay);
            assertNotNull(market);

            final Trans trans = new Trans();
            final Order order = s.placeOrder(accnt, market, "", Action.BUY, 12345, 5, 1, trans)
                    .getOrder();
            assertEquals(accnt.getTrader(), order.getTrader());
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

            s.reviseOrder(accnt, market, order, 4, trans);
            assertEquals(accnt.getTrader(), order.getTrader());
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.mock.MockModel;

public final class ServTest {

    private Model model;

    @Before
    public final void setUp() {
        model = new MockModel();
    }

    @After
    public final void tearDown() throws Exception {
        model.close();
        model = null;
    }

    @Test
    public final void test() throws BadRequestException, NotFoundException {
        final Serv serv = new Serv(model);
        final Sess sess = serv.getLazySess("MARAYL");
        assertNotNull(sess);

        final int settlDay = ymdToJd(2014, 2, 14);
        final int expiryDay = ymdToJd(2014, 2, 12);
        final long now = 1394625600000L;
        final Market market = serv.createMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD",
                settlDay, expiryDay, now);
        assertNotNull(market);

        final Trans trans = new Trans();
        final Order order = serv.placeOrder(sess, market, "", Action.BUY, 12345, 5, 1, now, trans)
                .getOrder();
        assertEquals(sess.getTrader(), order.getTrader());
        assertEquals(market.getMnem(), order.getMarket());
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
        assertEquals(now, order.getCreated());
        assertEquals(now, order.getModified());

        serv.reviseOrder(sess, market, order, 4, now + 1, trans);
        assertEquals(sess.getTrader(), order.getTrader());
        assertEquals(market.getMnem(), order.getMarket());
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
        assertEquals(now, order.getCreated());
        assertEquals(now + 1, order.getModified());
    }
}

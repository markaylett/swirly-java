/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.entity.BasicFactory;
import com.swirlycloud.twirly.entity.Factory;
import com.swirlycloud.twirly.entity.Order;

public final class OrderTest {
    private static final Factory FACTORY = new BasicFactory();

    private static final double DELTA = 0.000001;
    private static final int TODAY = ymdToJd(2014, 2, 12);
    private static final int SETTL_DAY = TODAY + 2;
    private static final long NOW = jdToMillis(TODAY);

    private static Order newOrder() {
        return FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, 1, "test", 0,
                Side.BUY, 10, 12345, 1, NOW);
    }

    @Test
    public final void testContruct() {
        final Order order = newOrder();

        assertEquals(1, order.getId());
        assertEquals(1, order.getOrderId());
        assertEquals("MARAYL", order.getTrader());
        assertEquals("EURUSD.MAR14", order.getMarket());
        assertEquals("EURUSD", order.getContr());
        assertEquals(SETTL_DAY, order.getSettlDay());
        assertEquals("test", order.getRef());
        assertEquals(State.NEW, order.getState());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(12345, order.getTicks());
        assertEquals(10, order.getLots());
        assertEquals(10, order.getResd());
        assertEquals(0, order.getExec());
        assertEquals(0, order.getCost());
        assertEquals(0, order.getLastTicks());
        assertEquals(0, order.getLastLots());
        assertEquals(1, order.getMinLots());
        assertFalse(order.isDone());
        assertFalse(order.isPecan());
        assertTrue(order.isWorking());
        assertEquals(NOW, order.getCreated());
        assertEquals(NOW, order.getModified());
    }

    @Test
    public final void testTrade() {
        final Order order = newOrder();
        order.trade(2, 12344, NOW + 1);

        assertEquals("MARAYL", order.getTrader());
        assertEquals("EURUSD.MAR14", order.getMarket());
        assertEquals("EURUSD", order.getContr());
        assertEquals(SETTL_DAY, order.getSettlDay());
        assertEquals(1, order.getId());
        assertEquals("test", order.getRef());
        assertEquals(1, order.getOrderId());
        assertEquals(State.TRADE, order.getState());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(10, order.getLots());
        assertEquals(12345, order.getTicks());
        assertEquals(8, order.getResd());
        assertEquals(2, order.getExec());
        assertEquals(24688, order.getCost());
        assertEquals(12344, order.getAvgTicks(), DELTA);
        assertEquals(2, order.getLastLots());
        assertEquals(12344, order.getLastTicks());
        assertEquals(1, order.getMinLots());
        assertFalse(order.isDone());
        assertFalse(order.isPecan());
        assertTrue(order.isWorking());
        assertEquals(NOW, order.getCreated());
        assertEquals(NOW + 1, order.getModified());
    }

    @Test
    public final void testRevise() {
        final Order order = newOrder();
        order.trade(2, 12344, NOW + 1);
        order.revise(5, NOW + 2);

        assertEquals("MARAYL", order.getTrader());
        assertEquals("EURUSD.MAR14", order.getMarket());
        assertEquals("EURUSD", order.getContr());
        assertEquals(SETTL_DAY, order.getSettlDay());
        assertEquals(1, order.getId());
        assertEquals("test", order.getRef());
        assertEquals(1, order.getOrderId());
        assertEquals(State.REVISE, order.getState());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(5, order.getLots());
        assertEquals(12345, order.getTicks());
        assertEquals(3, order.getResd());
        assertEquals(2, order.getExec());
        assertEquals(24688, order.getCost());
        assertEquals(12344, order.getAvgTicks(), DELTA);
        assertEquals(2, order.getLastLots());
        assertEquals(12344, order.getLastTicks());
        assertEquals(1, order.getMinLots());
        assertFalse(order.isDone());
        assertFalse(order.isPecan());
        assertTrue(order.isWorking());
        assertEquals(NOW, order.getCreated());
        assertEquals(NOW + 2, order.getModified());
    }

    @Test
    public final void testCancel() {
        final Order order = newOrder();
        order.trade(2, 12344, NOW + 1);
        order.cancel(NOW + 2);

        assertEquals("MARAYL", order.getTrader());
        assertEquals("EURUSD.MAR14", order.getMarket());
        assertEquals("EURUSD", order.getContr());
        assertEquals(SETTL_DAY, order.getSettlDay());
        assertEquals(1, order.getId());
        assertEquals("test", order.getRef());
        assertEquals(1, order.getOrderId());
        assertEquals(State.CANCEL, order.getState());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(10, order.getLots());
        assertEquals(12345, order.getTicks());
        assertEquals(0, order.getResd());
        assertEquals(2, order.getExec());
        assertEquals(24688, order.getCost());
        assertEquals(12344, order.getAvgTicks(), DELTA);
        assertEquals(2, order.getLastLots());
        assertEquals(12344, order.getLastTicks());
        assertEquals(1, order.getMinLots());
        assertTrue(order.isDone());
        assertFalse(order.isPecan());
        assertFalse(order.isWorking());
        assertEquals(NOW, order.getCreated());
        assertEquals(NOW + 2, order.getModified());
    }

    @Test
    public final void testMulti() {
        final Order order = newOrder();
        order.trade(2, 12344, NOW + 1);
        order.trade(3, 12345, NOW + 2);

        assertEquals("MARAYL", order.getTrader());
        assertEquals("EURUSD.MAR14", order.getMarket());
        assertEquals("EURUSD", order.getContr());
        assertEquals(SETTL_DAY, order.getSettlDay());
        assertEquals(1, order.getId());
        assertEquals("test", order.getRef());
        assertEquals(1, order.getOrderId());
        assertEquals(State.TRADE, order.getState());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(10, order.getLots());
        assertEquals(12345, order.getTicks());
        assertEquals(5, order.getResd());
        assertEquals(5, order.getExec());
        assertEquals(61723, order.getCost());
        assertEquals(12344.6, order.getAvgTicks(), DELTA);
        assertEquals(3, order.getLastLots());
        assertEquals(12345, order.getLastTicks());
        assertEquals(1, order.getMinLots());
        assertFalse(order.isDone());
        assertFalse(order.isPecan());
        assertTrue(order.isWorking());
        assertEquals(NOW, order.getCreated());
        assertEquals(NOW + 2, order.getModified());
    }

    @Test
    public final void testToString() {
        final Order order = newOrder();
        assertEquals(
                "{\"trader\":\"MARAYL\",\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"id\":1,\"ref\":\"test\",\"quoteId\":0,\"state\":\"NEW\",\"side\":\"BUY\",\"lots\":10,\"ticks\":12345,\"resd\":10,\"exec\":0,\"cost\":0,\"lastLots\":null,\"lastTicks\":null,\"minLots\":1,\"pecan\":false,\"created\":1394625600000,\"modified\":1394625600000}",
                order.toString());
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.entity.BasicFactory;
import com.swirlycloud.twirly.entity.Exec;
import com.swirlycloud.twirly.entity.Factory;
import com.swirlycloud.twirly.entity.Order;

public final class ExecTest {

    private static final Factory FACTORY = new BasicFactory();

    private static final double DELTA = 0.000001;
    private static final int TODAY = ymdToJd(2014, 2, 12);
    private static final int SETTL_DAY = TODAY + 2;
    private static final long NOW = jdToMillis(TODAY);

    private static @NonNull Order newOrder() {
        return FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, 1, "test", Side.BUY,
                10, 12345, 1, NOW);
    }

    @Test
    public final void testContruct() {
        final Order order = newOrder();
        final Exec exec = FACTORY.newExec(order, 2, NOW + 1);

        assertEquals(2, exec.getId());
        assertEquals(1, exec.getOrderId());
        assertEquals("MARAYL", exec.getTrader());
        assertEquals("EURUSD.MAR14", exec.getMarket());
        assertEquals("EURUSD", exec.getContr());
        assertEquals(SETTL_DAY, exec.getSettlDay());
        assertEquals("test", exec.getRef());
        assertEquals(State.NEW, exec.getState());
        assertEquals(Side.BUY, exec.getSide());
        assertEquals(12345, exec.getTicks());
        assertEquals(10, exec.getLots());
        assertEquals(10, exec.getResd());
        assertEquals(0, exec.getExec());
        assertEquals(0, exec.getCost());
        assertEquals(0, exec.getLastTicks());
        assertEquals(0, exec.getLastLots());
        assertEquals(1, exec.getMinLots());
        assertFalse(exec.isDone());
        assertEquals(NOW + 1, exec.getCreated());
    }

    @Test
    public final void testTrade() {
        final Order order = newOrder();
        final Exec exec = FACTORY.newExec(order, 2, NOW + 1);
        exec.trade(2, 12344, 3, Role.MAKER, "GOSAYL");

        assertEquals("MARAYL", exec.getTrader());
        assertEquals("EURUSD.MAR14", exec.getMarket());
        assertEquals("EURUSD", exec.getContr());
        assertEquals(SETTL_DAY, exec.getSettlDay());
        assertEquals(2, exec.getId());
        assertEquals("test", exec.getRef());
        assertEquals(1, exec.getOrderId());
        assertEquals(State.TRADE, exec.getState());
        assertEquals(Side.BUY, exec.getSide());
        assertEquals(10, exec.getLots());
        assertEquals(12345, exec.getTicks());
        assertEquals(8, exec.getResd());
        assertEquals(2, exec.getExec());
        assertEquals(24688, exec.getCost());
        assertEquals(12344, exec.getAvgTicks(), DELTA);
        assertEquals(2, exec.getLastLots());
        assertEquals(12344, exec.getLastTicks());
        assertEquals(1, exec.getMinLots());
        assertFalse(exec.isDone());
        assertEquals(3, exec.getMatchId());
        assertEquals(Role.MAKER, exec.getRole());
        assertEquals("GOSAYL", exec.getCpty());
        assertEquals(NOW + 1, exec.getCreated());
    }

    @Test
    public final void testInverse() {
        final Order order = newOrder();
        Exec exec = FACTORY.newExec(order, 2, NOW + 1);
        exec.trade(2, 12344, 3, Role.MAKER, "GOSAYL");
        exec = exec.inverse(3);

        assertEquals("GOSAYL", exec.getTrader());
        assertEquals("EURUSD.MAR14", exec.getMarket());
        assertEquals("EURUSD", exec.getContr());
        assertEquals(SETTL_DAY, exec.getSettlDay());
        assertEquals(3, exec.getId());
        assertEquals("test", exec.getRef());
        assertEquals(1, exec.getOrderId());
        assertEquals(State.TRADE, exec.getState());
        assertEquals(Side.SELL, exec.getSide());
        assertEquals(10, exec.getLots());
        assertEquals(12345, exec.getTicks());
        assertEquals(8, exec.getResd());
        assertEquals(2, exec.getExec());
        assertEquals(24688, exec.getCost());
        assertEquals(12344, exec.getAvgTicks(), DELTA);
        assertEquals(2, exec.getLastLots());
        assertEquals(12344, exec.getLastTicks());
        assertEquals(1, exec.getMinLots());
        assertFalse(exec.isDone());
        assertEquals(3, exec.getMatchId());
        assertEquals(Role.TAKER, exec.getRole());
        assertEquals("MARAYL", exec.getCpty());
        assertEquals(NOW + 1, exec.getCreated());
    }

    @Test
    public final void testRevise() {
        final Order order = newOrder();
        final Exec exec = FACTORY.newExec(order, 2, NOW + 1);
        exec.trade(2, 12344, 3, Role.MAKER, "GOSAYL");
        exec.revise(5);

        assertEquals("MARAYL", exec.getTrader());
        assertEquals("EURUSD.MAR14", exec.getMarket());
        assertEquals("EURUSD", exec.getContr());
        assertEquals(SETTL_DAY, exec.getSettlDay());
        assertEquals(2, exec.getId());
        assertEquals("test", exec.getRef());
        assertEquals(1, exec.getOrderId());
        assertEquals(State.REVISE, exec.getState());
        assertEquals(Side.BUY, exec.getSide());
        assertEquals(5, exec.getLots());
        assertEquals(12345, exec.getTicks());
        assertEquals(3, exec.getResd());
        assertEquals(2, exec.getExec());
        assertEquals(24688, exec.getCost());
        assertEquals(12344, exec.getAvgTicks(), DELTA);
        assertEquals(2, exec.getLastLots());
        assertEquals(12344, exec.getLastTicks());
        assertEquals(1, exec.getMinLots());
        assertFalse(exec.isDone());
        assertEquals(3, exec.getMatchId());
        assertEquals(Role.MAKER, exec.getRole());
        assertEquals("GOSAYL", exec.getCpty());
        assertEquals(NOW + 1, exec.getCreated());
    }

    @Test
    public final void testCancel() {
        final Order order = newOrder();
        final Exec exec = FACTORY.newExec(order, 2, NOW + 1);
        exec.trade(2, 12344, 3, Role.MAKER, "GOSAYL");
        exec.cancel(order.quotd);

        assertEquals("MARAYL", exec.getTrader());
        assertEquals("EURUSD.MAR14", exec.getMarket());
        assertEquals("EURUSD", exec.getContr());
        assertEquals(SETTL_DAY, exec.getSettlDay());
        assertEquals(2, exec.getId());
        assertEquals("test", exec.getRef());
        assertEquals(1, exec.getOrderId());
        assertEquals(State.CANCEL, exec.getState());
        assertEquals(Side.BUY, exec.getSide());
        assertEquals(10, exec.getLots());
        assertEquals(12345, exec.getTicks());
        assertEquals(0, exec.getResd());
        assertEquals(2, exec.getExec());
        assertEquals(24688, exec.getCost());
        assertEquals(12344, exec.getAvgTicks(), DELTA);
        assertEquals(2, exec.getLastLots());
        assertEquals(12344, exec.getLastTicks());
        assertEquals(1, exec.getMinLots());
        assertTrue(exec.isDone());
        assertEquals(3, exec.getMatchId());
        assertEquals(Role.MAKER, exec.getRole());
        assertEquals("GOSAYL", exec.getCpty());
        assertEquals(NOW + 1, exec.getCreated());
    }

    @Test
    public final void testMulti() {
        final Order order = newOrder();
        final Exec exec = FACTORY.newExec(order, 2, NOW + 1);
        exec.trade(2, 12344, 3, Role.MAKER, "GOSAYL");
        exec.trade(3, 12345, 3, Role.MAKER, "GOSAYL");

        assertEquals("MARAYL", exec.getTrader());
        assertEquals("EURUSD.MAR14", exec.getMarket());
        assertEquals("EURUSD", exec.getContr());
        assertEquals(SETTL_DAY, exec.getSettlDay());
        assertEquals(2, exec.getId());
        assertEquals("test", exec.getRef());
        assertEquals(1, exec.getOrderId());
        assertEquals(State.TRADE, exec.getState());
        assertEquals(Side.BUY, exec.getSide());
        assertEquals(10, exec.getLots());
        assertEquals(12345, exec.getTicks());
        assertEquals(5, exec.getResd());
        assertEquals(5, exec.getExec());
        assertEquals(61723, exec.getCost());
        assertEquals(12344.6, exec.getAvgTicks(), DELTA);
        assertEquals(3, exec.getLastLots());
        assertEquals(12345, exec.getLastTicks());
        assertEquals(1, exec.getMinLots());
        assertFalse(exec.isDone());
        assertEquals(3, exec.getMatchId());
        assertEquals(Role.MAKER, exec.getRole());
        assertEquals("GOSAYL", exec.getCpty());
        assertEquals(NOW + 1, exec.getCreated());
    }

    @Test
    public final void testToString() {
        final Order order = FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD",
                JulianDay.isoToJd(20140314), 1, "test", Side.BUY, 3, 12345, 1, 1414692516006L);
        final Exec exec = FACTORY.newExec(order, 2, 1414692516007L);
        assertEquals(
                "{\"trader\":\"MARAYL\",\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"id\":2,\"ref\":\"test\",\"orderId\":1,\"state\":\"NEW\",\"side\":\"BUY\",\"lots\":3,\"ticks\":12345,\"resd\":3,\"exec\":0,\"cost\":0,\"lastLots\":null,\"lastTicks\":null,\"minLots\":1,\"matchId\":null,\"role\":null,\"cpty\":null,\"created\":1414692516007}",
                exec.toString());
        exec.trade(1, 12345, 3, Role.MAKER, "GOSAYL");
        assertEquals(
                "{\"trader\":\"MARAYL\",\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"id\":2,\"ref\":\"test\",\"orderId\":1,\"state\":\"TRADE\",\"side\":\"BUY\",\"lots\":3,\"ticks\":12345,\"resd\":2,\"exec\":1,\"cost\":12345,\"lastLots\":1,\"lastTicks\":12345,\"minLots\":1,\"matchId\":3,\"role\":\"MAKER\",\"cpty\":\"GOSAYL\",\"created\":1414692516007}",
                exec.toString());
    }
}

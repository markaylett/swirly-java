/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.util.MnemUtil.newMnem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Level;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.mock.MockModel;
import com.swirlycloud.twirly.node.SlNode;

public final class ServTest {

    private static final double DELTA = 0.1;
    private static final String TRADER = "MARAYL";
    private static final int TODAY = ymdToJd(2014, 2, 11);
    private static final int SETTL_DAY = TODAY + 2;
    private static final int EXPIRY_DAY = TODAY + 1;
    private static final int STATE = 0x01;

    private static final long NOW = jdToMillis(TODAY);

    private Model model;

    @Before
    public final void setUp() {
        model = new MockModel() {
            @Override
            public final SlNode selectMarket() {
                return new Market("EURUSD.MAR14", "EURUSD March 14", newMnem("EURUSD"), SETTL_DAY,
                        EXPIRY_DAY, STATE, 12345, 10, NOW - 2, 3, 2);
            }

            @Override
            public final SlNode selectOrder() {
                final Order first = new Order(1, TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY,
                        "first", Action.BUY, 12344, 11, 1, NOW - 5);
                final Order second = new Order(2, TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY,
                        "second", Action.BUY, 12345, 10, 1, NOW - 4);
                final Order third = new Order(3, TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY,
                        "third", Action.SELL, 12346, 10, 1, NOW - 3);
                // Fully fill second order.
                second.trade(12345, 10, NOW - 2);
                // Partially fill third order.
                third.trade(12346, 7, NOW - 1);
                first.setSlNext(second);
                second.setSlNext(third);
                return first;
            }

            @Override
            public final SlNode selectTrade() {
                final Exec second = new Exec(1, 2, TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY,
                        "second", State.TRADE, Action.BUY, 12345, 10, 0, 10, 123450, 12345, 10, 1,
                        1, Role.MAKER, "RAMMAC", NOW - 2);
                final Exec third = new Exec(2, 3, TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY,
                        "third", State.TRADE, Action.SELL, 12346, 10, 3, 7, 86422, 12346, 7, 1, 2,
                        Role.TAKER, "RAMMAC", NOW - 1);
                second.setSlNext(third);
                return second;
            }
        };
    }

    @After
    public final void tearDown() throws Exception {
        model.close();
        model = null;
    }

    @Test
    public final void testFindMarket() throws Exception {
        final Serv serv = new Serv(model);
        final Market actual = (Market) serv.findRec(RecType.MARKET, "EURUSD.MAR14");

        assertNotNull(actual);
        assertEquals("EURUSD.MAR14", actual.getMnem());
        assertEquals("EURUSD March 14", actual.getDisplay());
        assertEquals("EURUSD", actual.getContr());
        assertEquals(SETTL_DAY, actual.getSettlDay());
        assertEquals(EXPIRY_DAY, actual.getExpiryDay());
        assertEquals(STATE, actual.getState());
        assertEquals(12345, actual.getLastTicks());
        assertEquals(10, actual.getLastLots());
        assertEquals(NOW - 2, actual.getLastTime());
        assertEquals(3, actual.getMaxOrderId());
        assertEquals(2, actual.getMaxExecId());
    }

    @Test
    public final void testBidSide() throws Exception {
        final Serv serv = new Serv(model);
        final Market actual = (Market) serv.findRec(RecType.MARKET, "EURUSD.MAR14");
        assertNotNull(actual);
        final Side side = actual.getBidSide();
        assertNotNull(side);
        final Level level = (Level) side.getFirstLevel();
        assertNotNull(level);
        assertEquals(12344, level.getTicks());
        assertEquals(11, level.getLots());
        assertEquals(1, level.getCount());
    }

    @Test
    public final void testOfferSide() throws Exception {
        final Serv serv = new Serv(model);
        final Market actual = (Market) serv.findRec(RecType.MARKET, "EURUSD.MAR14");
        assertNotNull(actual);
        final Side side = actual.getOfferSide();
        assertNotNull(side);
        final Level level = (Level) side.getFirstLevel();
        assertNotNull(level);
        assertEquals(12346, level.getTicks());
        assertEquals(3, level.getLots());
        assertEquals(1, level.getCount());
    }

    @Test
    public final void testFindOrder() throws Exception {
        final Serv serv = new Serv(model);
        final Sess sess = serv.getLazySess(TRADER);
        assertNotNull(sess);

        Order actual = sess.findOrder("EURUSD.MAR14", 1);
        assertNotNull(actual);
        assertEquals(sess.findOrder("first"), actual);
        assertEquals(TRADER, actual.getTrader());
        assertEquals("EURUSD.MAR14", actual.getMarket());
        assertEquals("first", actual.getRef());
        assertEquals(State.NEW, actual.getState());
        assertEquals(Action.BUY, actual.getAction());
        assertEquals(12344, actual.getTicks());
        assertEquals(11, actual.getLots());
        assertEquals(11, actual.getResd());
        assertEquals(0, actual.getExec());
        assertEquals(0, actual.getCost());
        assertEquals(0.0, actual.getAvgTicks(), DELTA);
        assertEquals(0, actual.getLastTicks());
        assertEquals(0, actual.getLastLots());
        assertEquals(1, actual.getMinLots());
        assertEquals(NOW - 5, actual.getCreated());
        assertEquals(NOW - 5, actual.getModified());

        actual = sess.findOrder("EURUSD.MAR14", 2);
        assertNotNull(actual);
        assertEquals(sess.findOrder("second"), actual);
        assertEquals(TRADER, actual.getTrader());
        assertEquals("EURUSD.MAR14", actual.getMarket());
        assertEquals("second", actual.getRef());
        assertEquals(State.TRADE, actual.getState());
        assertEquals(Action.BUY, actual.getAction());
        assertEquals(12345, actual.getTicks());
        assertEquals(10, actual.getLots());
        assertEquals(0, actual.getResd());
        assertEquals(10, actual.getExec());
        assertEquals(123450, actual.getCost());
        assertEquals(12345, actual.getLastTicks());
        assertEquals(12345.0, actual.getAvgTicks(), DELTA);
        assertEquals(10, actual.getLastLots());
        assertEquals(1, actual.getMinLots());
        assertEquals(NOW - 4, actual.getCreated());
        assertEquals(NOW - 2, actual.getModified());

        actual = sess.findOrder("EURUSD.MAR14", 3);
        assertNotNull(actual);
        assertEquals(sess.findOrder("third"), actual);
        assertEquals(TRADER, actual.getTrader());
        assertEquals("EURUSD.MAR14", actual.getMarket());
        assertEquals("third", actual.getRef());
        assertEquals(State.TRADE, actual.getState());
        assertEquals(Action.SELL, actual.getAction());
        assertEquals(12346, actual.getTicks());
        assertEquals(10, actual.getLots());
        assertEquals(3, actual.getResd());
        assertEquals(7, actual.getExec());
        assertEquals(86422, actual.getCost());
        assertEquals(12346, actual.getLastTicks());
        assertEquals(12346.0, actual.getAvgTicks(), DELTA);
        assertEquals(7, actual.getLastLots());
        assertEquals(1, actual.getMinLots());
        assertEquals(NOW - 3, actual.getCreated());
        assertEquals(NOW - 1, actual.getModified());
    }

    @Test
    public final void testFindTrade() throws Exception {
        final Serv serv = new Serv(model);
        final Sess sess = serv.getLazySess(TRADER);
        assertNotNull(sess);

        Exec actual = sess.findTrade("EURUSD.MAR14", 1);
        assertNotNull(actual);
        assertEquals(TRADER, actual.getTrader());
        assertEquals("EURUSD.MAR14", actual.getMarket());
        assertEquals("second", actual.getRef());
        assertEquals(State.TRADE, actual.getState());
        assertEquals(Action.BUY, actual.getAction());
        assertEquals(12345, actual.getTicks());
        assertEquals(10, actual.getLots());
        assertEquals(0, actual.getResd());
        assertEquals(10, actual.getExec());
        assertEquals(123450, actual.getCost());
        assertEquals(12345, actual.getLastTicks());
        assertEquals(12345.0, actual.getAvgTicks(), DELTA);
        assertEquals(10, actual.getLastLots());
        assertEquals(1, actual.getMinLots());
        assertTrue(actual.isDone());
        assertTrue(actual.isAuto());
        assertEquals(1, actual.getMatchId());
        assertEquals(Role.MAKER, actual.getRole());
        assertEquals("RAMMAC", actual.getCpty());
        assertEquals(NOW - 2, actual.getCreated());

        actual = sess.findTrade("EURUSD.MAR14", 2);
        assertNotNull(actual);
        assertEquals(TRADER, actual.getTrader());
        assertEquals("EURUSD.MAR14", actual.getMarket());
        assertEquals("third", actual.getRef());
        assertEquals(State.TRADE, actual.getState());
        assertEquals(Action.SELL, actual.getAction());
        assertEquals(12346, actual.getTicks());
        assertEquals(10, actual.getLots());
        assertEquals(3, actual.getResd());
        assertEquals(7, actual.getExec());
        assertEquals(86422, actual.getCost());
        assertEquals(12346, actual.getLastTicks());
        assertEquals(12346.0, actual.getAvgTicks(), DELTA);
        assertEquals(7, actual.getLastLots());
        assertEquals(1, actual.getMinLots());
        assertFalse(actual.isDone());
        assertTrue(actual.isAuto());
        assertEquals(2, actual.getMatchId());
        assertEquals(Role.TAKER, actual.getRole());
        assertEquals("RAMMAC", actual.getCpty());
        assertEquals(NOW - 1, actual.getCreated());
    }

    @Test(expected = BadRequestException.class)
    public final void testDuplicateMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException {
        final Serv serv = new Serv(model);
        serv.createMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", SETTL_DAY, EXPIRY_DAY,
                STATE, NOW);
    }

    @Test
    public final void testPlace() throws BadRequestException, NotFoundException,
            ServiceUnavailableException {
        final Serv serv = new Serv(model);
        final Sess sess = serv.getLazySess(TRADER);
        assertNotNull(sess);

        final Market market = (Market) serv.findRec(RecType.MARKET, "EURUSD.MAR14");
        assertNotNull(market);

        try (final Trans trans = new Trans()) {
            serv.placeOrder(sess, market, "", Action.BUY, 12345, 5, 1, NOW, trans);
            final Order order = trans.getOrder();
            assertEquals(sess.getTrader(), order.getTrader());
            assertEquals(market.getMnem(), order.getMarket());
            assertEquals("", order.getRef());
            assertEquals(State.NEW, order.getState());
            assertEquals(Action.BUY, order.getAction());
            assertEquals(12345, order.getTicks());
            assertEquals(5, order.getLots());
            assertEquals(5, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getCost());
            assertEquals(0, order.getLastTicks());
            assertEquals(0, order.getLastLots());
            assertEquals(1, order.getMinLots());
            assertEquals(NOW, order.getCreated());
            assertEquals(NOW, order.getModified());
        }
    }

    @Test
    public final void testRevise() throws BadRequestException, NotFoundException,
            ServiceUnavailableException {
        final Serv serv = new Serv(model);
        final Sess sess = serv.getLazySess(TRADER);
        assertNotNull(sess);

        final Market market = (Market) serv.findRec(RecType.MARKET, "EURUSD.MAR14");
        assertNotNull(market);

        try (final Trans trans = new Trans()) {
            serv.placeOrder(sess, market, "", Action.BUY, 12345, 5, 1, NOW, trans);
            final Order order = trans.getOrder();
            serv.reviseOrder(sess, market, order, 4, NOW + 1, trans);
            assertEquals(sess.getTrader(), order.getTrader());
            assertEquals(market.getMnem(), order.getMarket());
            assertNull(order.getRef());
            assertEquals(State.REVISE, order.getState());
            assertEquals(Action.BUY, order.getAction());
            assertEquals(12345, order.getTicks());
            assertEquals(4, order.getLots());
            assertEquals(4, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getCost());
            assertEquals(0, order.getLastTicks());
            assertEquals(0, order.getLastLots());
            assertEquals(1, order.getMinLots());
            assertEquals(NOW, order.getCreated());
            assertEquals(NOW + 1, order.getModified());
        }
    }

    @Test
    public final void testCancel() throws BadRequestException, NotFoundException,
            ServiceUnavailableException {
        final Serv serv = new Serv(model);
        final Sess sess = serv.getLazySess(TRADER);
        assertNotNull(sess);

        final Market market = (Market) serv.findRec(RecType.MARKET, "EURUSD.MAR14");
        assertNotNull(market);

        try (final Trans trans = new Trans()) {
            serv.placeOrder(sess, market, "", Action.BUY, 12345, 5, 1, NOW, trans);
            final Order order = trans.getOrder();
            serv.cancelOrder(sess, market, order, NOW + 1, trans);
            assertEquals(sess.getTrader(), order.getTrader());
            assertEquals(market.getMnem(), order.getMarket());
            assertNull(order.getRef());
            assertEquals(State.CANCEL, order.getState());
            assertEquals(Action.BUY, order.getAction());
            assertEquals(12345, order.getTicks());
            assertEquals(5, order.getLots());
            assertEquals(0, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getCost());
            assertEquals(0, order.getLastTicks());
            assertEquals(0, order.getLastLots());
            assertEquals(1, order.getMinLots());
            assertEquals(NOW, order.getCreated());
            assertEquals(NOW + 1, order.getModified());
        }
    }
}

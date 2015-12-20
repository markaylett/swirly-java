/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.app;

import static com.swirlycloud.swirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.swirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.swirly.io.CacheUtil.NO_CACHE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.swirlycloud.swirly.book.BookSide;
import com.swirlycloud.swirly.book.Level;
import com.swirlycloud.swirly.book.MarketBook;
import com.swirlycloud.swirly.domain.Role;
import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.domain.State;
import com.swirlycloud.swirly.entity.Exec;
import com.swirlycloud.swirly.entity.Factory;
import com.swirlycloud.swirly.entity.Order;
import com.swirlycloud.swirly.entity.RecTree;
import com.swirlycloud.swirly.entity.RecType;
import com.swirlycloud.swirly.entity.TraderSess;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.io.Datastore;
import com.swirlycloud.swirly.mock.MockDatastore;
import com.swirlycloud.swirly.node.SlNode;

@SuppressWarnings("null")
public final class ServTest {

    private static final double DELTA = 0.1;
    private static final String TRADER = "MARAYL";
    private static final int TODAY = ymdToJd(2014, 2, 11);
    private static final int SETTL_DAY = TODAY + 2;
    private static final int EXPIRY_DAY = TODAY + 1;
    private static final int STATE = 0x01;

    private static final long NOW = jdToMillis(TODAY);

    private Serv serv;

    private static Datastore newDatastore() {
        return new MockDatastore() {

            @Override
            public final @NonNull RecTree readMarket(@NonNull Factory factory) {
                final RecTree t = new RecTree();
                t.insert(factory.newMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", SETTL_DAY,
                        EXPIRY_DAY, STATE, 10, 12345, NOW - 2, 3, 2, 4));
                return t;
            }

            @Override
            public final SlNode readOrder(@NonNull Factory factory) {
                final Order first = factory.newOrder(TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY, 1,
                        "first", 0, Side.BUY, 11, 12344, 1, NOW - 5);
                final Order second = factory.newOrder(TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY,
                        2, "second", 0, Side.BUY, 10, 12345, 1, NOW - 4);
                final Order third = factory.newOrder(TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY, 3,
                        "third", 0, Side.SELL, 10, 12346, 1, NOW - 3);
                // Fully fill second order.
                second.trade(10, 12345, NOW - 2);
                // Partially fill third order.
                third.trade(7, 12346, NOW - 1);
                first.setSlNext(second);
                second.setSlNext(third);
                return first;
            }

            @Override
            public final SlNode readTrade(@NonNull Factory factory) {
                final Exec second = factory.newExec(TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY, 1,
                        "second", 2, 0, State.TRADE, Side.BUY, 10, 12345, 0, 10, 123450, 10, 12345,
                        1, 1, Role.MAKER, "RAMMAC", NOW - 2);
                final Exec third = factory.newExec(TRADER, "EURUSD.MAR14", "EURUSD", SETTL_DAY, 2,
                        "third", 3, 0, State.TRADE, Side.SELL, 10, 12346, 3, 7, 86422, 7, 12346, 1,
                        2, Role.TAKER, "RAMMAC", NOW - 1);
                second.setSlNext(third);
                return second;
            }
        };
    }

    @Before
    public final void setUp() throws Exception {
        serv = new Serv(newDatastore(), NO_CACHE, NOW);
    }

    @After
    public final void tearDown() throws Exception {
        // Assumption: MockDatastore need not be closed because it does not acquire resources.
        serv = null;
    }

    @Test
    public final void testFindMarket() throws Exception {
        final MarketBook actual = (MarketBook) serv.findRec(RecType.MARKET, "EURUSD.MAR14");

        assertNotNull(actual);
        assertEquals("EURUSD.MAR14", actual.getMnem());
        assertEquals("EURUSD March 14", actual.getDisplay());
        assertEquals("EURUSD", actual.getContr());
        assertEquals(SETTL_DAY, actual.getSettlDay());
        assertEquals(EXPIRY_DAY, actual.getExpiryDay());
        assertEquals(STATE, actual.getState());
        assertEquals(10, actual.getLastLots());
        assertEquals(12345, actual.getLastTicks());
        assertEquals(NOW - 2, actual.getLastTime());
        assertEquals(3, actual.getMaxOrderId());
        assertEquals(2, actual.getMaxExecId());
    }

    @Test
    public final void testBidSide() throws Exception {
        final MarketBook actual = serv.getMarket("EURUSD.MAR14");
        assertNotNull(actual);
        final BookSide side = actual.getBidSide();
        assertNotNull(side);
        final Level level = (Level) side.getFirstLevel();
        assertNotNull(level);
        assertEquals(12344, level.getTicks());
        assertEquals(11, level.getResd());
        assertEquals(1, level.getCount());
    }

    @Test
    public final void testOfferSide() throws Exception {
        final MarketBook actual = serv.getMarket("EURUSD.MAR14");
        assertNotNull(actual);
        final BookSide bookSide = actual.getOfferSide();
        assertNotNull(bookSide);
        final Level level = (Level) bookSide.getFirstLevel();
        assertNotNull(level);
        assertEquals(12346, level.getTicks());
        assertEquals(3, level.getResd());
        assertEquals(1, level.getCount());
    }

    @Test
    public final void testFindOrder() throws Exception {
        final TraderSess sess = serv.getTrader(TRADER);
        assertNotNull(sess);

        Order actual = sess.findOrder("EURUSD.MAR14", 1);
        assertNotNull(actual);
        assertEquals(sess.findOrder("first"), actual);
        assertEquals(TRADER, actual.getTrader());
        assertEquals("EURUSD.MAR14", actual.getMarket());
        assertEquals("first", actual.getRef());
        assertEquals(State.NEW, actual.getState());
        assertEquals(Side.BUY, actual.getSide());
        assertEquals(11, actual.getLots());
        assertEquals(12344, actual.getTicks());
        assertEquals(11, actual.getResd());
        assertEquals(0, actual.getExec());
        assertEquals(0, actual.getCost());
        assertEquals(0.0, actual.getAvgTicks(), DELTA);
        assertEquals(0, actual.getLastLots());
        assertEquals(0, actual.getLastTicks());
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
        assertEquals(Side.BUY, actual.getSide());
        assertEquals(10, actual.getLots());
        assertEquals(12345, actual.getTicks());
        assertEquals(0, actual.getResd());
        assertEquals(10, actual.getExec());
        assertEquals(123450, actual.getCost());
        assertEquals(12345.0, actual.getAvgTicks(), DELTA);
        assertEquals(10, actual.getLastLots());
        assertEquals(12345, actual.getLastTicks());
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
        assertEquals(Side.SELL, actual.getSide());
        assertEquals(10, actual.getLots());
        assertEquals(12346, actual.getTicks());
        assertEquals(3, actual.getResd());
        assertEquals(7, actual.getExec());
        assertEquals(86422, actual.getCost());
        assertEquals(12346.0, actual.getAvgTicks(), DELTA);
        assertEquals(7, actual.getLastLots());
        assertEquals(12346, actual.getLastTicks());
        assertEquals(1, actual.getMinLots());
        assertEquals(NOW - 3, actual.getCreated());
        assertEquals(NOW - 1, actual.getModified());
    }

    @Test
    public final void testFindTrade() throws Exception {
        final TraderSess sess = serv.getTrader(TRADER);
        assertNotNull(sess);

        Exec actual = sess.findTrade("EURUSD.MAR14", 1);
        assertNotNull(actual);
        assertEquals(TRADER, actual.getTrader());
        assertEquals("EURUSD.MAR14", actual.getMarket());
        assertEquals("second", actual.getRef());
        assertEquals(State.TRADE, actual.getState());
        assertEquals(Side.BUY, actual.getSide());
        assertEquals(10, actual.getLots());
        assertEquals(12345, actual.getTicks());
        assertEquals(0, actual.getResd());
        assertEquals(10, actual.getExec());
        assertEquals(123450, actual.getCost());
        assertEquals(12345.0, actual.getAvgTicks(), DELTA);
        assertEquals(10, actual.getLastLots());
        assertEquals(12345, actual.getLastTicks());
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
        assertEquals(Side.SELL, actual.getSide());
        assertEquals(10, actual.getLots());
        assertEquals(12346, actual.getTicks());
        assertEquals(3, actual.getResd());
        assertEquals(7, actual.getExec());
        assertEquals(86422, actual.getCost());
        assertEquals(12346.0, actual.getAvgTicks(), DELTA);
        assertEquals(7, actual.getLastLots());
        assertEquals(12346, actual.getLastTicks());
        assertEquals(1, actual.getMinLots());
        assertFalse(actual.isDone());
        assertTrue(actual.isAuto());
        assertEquals(2, actual.getMatchId());
        assertEquals(Role.TAKER, actual.getRole());
        assertEquals("RAMMAC", actual.getCpty());
        assertEquals(NOW - 1, actual.getCreated());
    }

    @Test(expected = BadRequestException.class)
    public final void testDuplicateMarket()
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        serv.createMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", SETTL_DAY, EXPIRY_DAY, STATE,
                NOW);
    }

    @Test
    public final void testCreate()
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final TraderSess sess = serv.getTrader(TRADER);
        assertNotNull(sess);

        final MarketBook book = serv.getMarket("EURUSD.MAR14");
        assertNotNull(book);

        try (final Result result = new Result()) {
            serv.createOrder(sess, book, "", 0, Side.BUY, 5, 12345, 1, NOW, result);
            final Order order = (Order) result.getFirstOrder();
            assertNotNull(order);
            assertEquals(sess.getMnem(), order.getTrader());
            assertEquals(book.getMnem(), order.getMarket());
            assertNull(order.getRef());
            assertEquals(State.NEW, order.getState());
            assertEquals(Side.BUY, order.getSide());
            assertEquals(5, order.getLots());
            assertEquals(12345, order.getTicks());
            assertEquals(5, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getCost());
            assertEquals(0, order.getLastLots());
            assertEquals(0, order.getLastTicks());
            assertEquals(1, order.getMinLots());
            assertEquals(NOW, order.getCreated());
            assertEquals(NOW, order.getModified());
        }
    }

    @Test
    public final void testRevise()
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final TraderSess sess = serv.getTrader(TRADER);
        assertNotNull(sess);

        final MarketBook book = serv.getMarket("EURUSD.MAR14");
        assertNotNull(book);

        try (final Result result = new Result()) {
            serv.createOrder(sess, book, "", 0, Side.BUY, 5, 12345, 1, NOW, result);
            final Order order = (Order) result.getFirstOrder();
            assertNotNull(order);
            serv.reviseOrder(sess, book, order, 4, NOW + 1, result);
            assertEquals(sess.getMnem(), order.getTrader());
            assertEquals(book.getMnem(), order.getMarket());
            assertNull(order.getRef());
            assertEquals(State.REVISE, order.getState());
            assertEquals(Side.BUY, order.getSide());
            assertEquals(4, order.getLots());
            assertEquals(12345, order.getTicks());
            assertEquals(4, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getCost());
            assertEquals(0, order.getLastLots());
            assertEquals(0, order.getLastTicks());
            assertEquals(1, order.getMinLots());
            assertEquals(NOW, order.getCreated());
            assertEquals(NOW + 1, order.getModified());
        }
    }

    @Test
    public final void testCancel()
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final TraderSess sess = serv.getTrader(TRADER);
        assertNotNull(sess);

        final MarketBook book = serv.getMarket("EURUSD.MAR14");
        assertNotNull(book);

        try (final Result result = new Result()) {
            serv.createOrder(sess, book, "", 0, Side.BUY, 5, 12345, 1, NOW, result);
            final Order order = (Order) result.getFirstOrder();
            assertNotNull(order);
            serv.cancelOrder(sess, book, order, NOW + 1, result);
            assertEquals(sess.getMnem(), order.getTrader());
            assertEquals(book.getMnem(), order.getMarket());
            assertNull(order.getRef());
            assertEquals(State.CANCEL, order.getState());
            assertEquals(Side.BUY, order.getSide());
            assertEquals(5, order.getLots());
            assertEquals(12345, order.getTicks());
            assertEquals(0, order.getResd());
            assertEquals(0, order.getExec());
            assertEquals(0, order.getCost());
            assertEquals(0, order.getLastLots());
            assertEquals(0, order.getLastTicks());
            assertEquals(1, order.getMinLots());
            assertEquals(NOW, order.getCreated());
            assertEquals(NOW + 1, order.getModified());
        }
    }
}

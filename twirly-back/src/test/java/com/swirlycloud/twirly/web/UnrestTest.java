/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_EXPIRED;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.domain.View;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.mock.MockAsset;
import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.mock.MockModel;
import com.swirlycloud.twirly.mock.MockTrader;
import com.swirlycloud.twirly.web.Unrest.AccntStruct;
import com.swirlycloud.twirly.web.Unrest.RecStruct;
import com.swirlycloud.twirly.web.Unrest.TransStruct;

public final class UnrestTest {

    private static final String EMAIL = "mark.aylett@gmail.com";
    private static final int TODAY = ymdToJd(2014, 2, 11);
    private static final int EXPIRY_DAY = TODAY + 1;
    private static final int FIXING_DAY = TODAY + 2;
    private static final int SETTL_DAY = TODAY + 3;

    private static final long NOW = jdToMillis(TODAY);

    private static void assertAsset(String mnem, Asset actual) {
        assertEquals(MockAsset.newAsset(mnem), actual);
    }

    private static void assertAssets(final Map<String, ? super Asset> assets) {
        MockAsset.selectAsset(new UnaryCallback<Asset>() {
            @Override
            public final void call(Asset arg) {
                assertEquals(arg, assets.get(arg.getMnem()));
            }
        });
    }

    private static void assertContr(String mnem, Contr actual) {
        assertEquals(MockContr.newContr(mnem), actual);
    }

    private static void assertContrs(final Map<String, ? super Contr> contrs) {
        MockContr.selectContr(new UnaryCallback<Contr>() {
            @Override
            public final void call(Contr arg) {
                assertEquals(arg, contrs.get(arg.getMnem()));
            }
        });
    }

    private static void assertTrader(String mnem, Trader actual) {
        assertEquals(MockTrader.newTrader(mnem), actual);
    }

    private static void assertTraders(final Map<String, ? super Trader> traders) {
        MockTrader.selectTrader(new UnaryCallback<Trader>() {
            @Override
            public final void call(Trader arg) {
                assertEquals(arg, traders.get(arg.getMnem()));
            }
        });
    }

    private static void assertView(String cmnem, View view) throws NotFoundException, IOException {
        assertEquals(MockContr.newContr(cmnem).getId(), view.getContrId());
        assertEquals(SETTL_DAY, view.getSettlDay());
        assertEquals(FIXING_DAY, view.getFixingDay());
        assertEquals(EXPIRY_DAY, view.getExpiryDay());

        assertEquals(0, view.getOfferTicks(0));
        assertEquals(0, view.getOfferLots(0));
        assertEquals(0, view.getOfferCount(0));

        assertEquals(0, view.getBidTicks(0));
        assertEquals(0, view.getBidLots(0));
        assertEquals(0, view.getOfferCount(0));

        assertEquals(0, view.getLastTicks());
        assertEquals(0, view.getLastLots());
        assertEquals(0, view.getLastTime());
    }

    private static void assertOrder(String cmnem, State state, Action action, long ticks,
            long lots, long resd, long exec, long lastTicks, long lastLots, Order order) {
        assertNotNull(order);
        assertEquals(1, order.getTraderId());
        assertEquals(MockContr.newContr(cmnem).getId(), order.getContrId());
        assertEquals(SETTL_DAY, order.getSettlDay());
        assertEquals("", order.getRef());
        assertEquals(state, order.getState());
        assertEquals(action, order.getAction());
        assertEquals(ticks, order.getTicks());
        assertEquals(lots, order.getLots());
        assertEquals(resd, order.getResd());
        assertEquals(exec, order.getExec());
        assertEquals(lastTicks, order.getLastTicks());
        assertEquals(lastLots, order.getLastLots());
        assertEquals(1, order.getMinLots());
        assertEquals(NOW, order.getCreated());
        assertEquals(NOW, order.getModified());
    }

    private static void assertExec(String cmnem, State state, Action action, long ticks, long lots,
            long resd, long exc, long lastTicks, long lastLots, Role role, Exec exec) {
        assertNotNull(exec);
        assertEquals(1, exec.getTraderId());
        assertEquals(12, exec.getContrId());
        assertEquals(SETTL_DAY, exec.getSettlDay());
        assertEquals("", exec.getRef());
        assertEquals(state, exec.getState());
        assertEquals(action, exec.getAction());
        assertEquals(ticks, exec.getTicks());
        assertEquals(lots, exec.getLots());
        assertEquals(resd, exec.getResd());
        assertEquals(exc, exec.getExec());
        assertEquals(lastTicks, exec.getLastTicks());
        assertEquals(lastLots, exec.getLastLots());
        assertEquals(1, exec.getMinLots());
        assertEquals(role, exec.getRole());
        assertEquals(1, exec.getCptyId());
        assertEquals(NOW, exec.getCreated());
    }

    @SuppressWarnings("unused")
    private static void assertPosn(Posn posn) {
        assertNotNull(posn);
    }

    private Unrest unrest;

    private final Trader postTrader(String mnem, String display, String email)
            throws BadRequestException, IOException {
        return unrest.postTrader(mnem, display, email, PARAMS_NONE, NOW);
    }

    private final View postMarket(String cmnem) throws BadRequestException, NotFoundException,
            IOException {
        return unrest.postMarket(cmnem, jdToIso(SETTL_DAY), jdToIso(FIXING_DAY),
                jdToIso(EXPIRY_DAY), PARAMS_NONE, NOW);
    }

    private final void deleteOrder(String cmnem, long id) throws BadRequestException,
            NotFoundException, IOException {
        unrest.deleteOrder(EMAIL, cmnem, jdToIso(SETTL_DAY), id, NOW);
    }

    private final TransStruct postOrder(String cmnem, Action action, long ticks, long lots)
            throws BadRequestException, NotFoundException, IOException {
        return unrest.postOrder(EMAIL, cmnem, jdToIso(SETTL_DAY), null, action, ticks, lots, 1,
                PARAMS_NONE, NOW);
    }

    private final TransStruct putOrder(String cmnem, long id, long lots)
            throws BadRequestException, NotFoundException, IOException {
        return unrest.putOrder(EMAIL, cmnem, jdToIso(SETTL_DAY), id, lots, PARAMS_NONE, NOW);
    }

    private final void deleteTrade(String email, String cmnem, int settlDate, long id)
            throws BadRequestException, NotFoundException {
        unrest.deleteTrade(EMAIL, cmnem, jdToIso(SETTL_DAY), id, NOW);
    }

    @Before
    public final void setUp() {
        unrest = new Unrest(new MockModel());
    }

    @After
    public final void tearDown() {
        unrest = null;
    }

    @Test
    public final void testGetRec() throws IOException {

        // With traders.
        RecStruct st = unrest.getRec(true, PARAMS_NONE, NOW);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertTraders(st.traders);

        // Without traders.
        st = unrest.getRec(false, PARAMS_NONE, NOW);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertTrue(st.traders.isEmpty());
    }

    @Test
    public final void testGetRecType() throws IOException {

        Map<String, Rec> recs = unrest.getRec(RecType.ASSET, PARAMS_NONE, NOW);
        assertAssets(recs);

        recs = unrest.getRec(RecType.CONTR, PARAMS_NONE, NOW);
        assertContrs(recs);

        recs = unrest.getRec(RecType.TRADER, PARAMS_NONE, NOW);
        assertTraders(recs);
    }

    @Test
    public final void testGetRecTypeMnem() throws NotFoundException, IOException {

        final Asset asset = (Asset) unrest.getRec(RecType.ASSET, "JPY", PARAMS_NONE, NOW);
        assertAsset("JPY", asset);
        try {
            unrest.getRec(RecType.ASSET, "JPYx", PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        final Contr contr = (Contr) unrest.getRec(RecType.CONTR, "USDJPY", PARAMS_NONE, NOW);
        assertContr("USDJPY", contr);
        try {
            unrest.getRec(RecType.CONTR, "USDJPYx", PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        final Trader trader = (Trader) unrest.getRec(RecType.TRADER, "MARAYL", PARAMS_NONE, NOW);
        assertTrader("MARAYL", trader);
        try {
            unrest.getRec(RecType.TRADER, "MARAYLx", PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testPostTrader() throws BadRequestException, NotFoundException, IOException {

        Trader trader = postTrader("MARAYL2", "Mark Aylett", "mark.aylett@swirlycloud.com");
        for (int i = 0; i < 2; ++i) {
            assertNotNull(trader);
            assertEquals("MARAYL2", trader.getMnem());
            assertEquals("Mark Aylett", trader.getDisplay());
            assertEquals("mark.aylett@swirlycloud.com", trader.getEmail());
            trader = (Trader) unrest.getRec(RecType.TRADER, "MARAYL2", PARAMS_NONE, NOW);
        }

        // Duplicate mnemonic.
        try {
            postTrader("MARAYL", "Mark Aylett", "mark.aylett@swirlycloud.com");
            fail("Expected exception");
        } catch (final BadRequestException e) {
        }

        // Duplicate email.
        try {
            postTrader("MARAYL3", "Mark Aylett", "mark.aylett@gmail.com");
            fail("Expected exception");
        } catch (final BadRequestException e) {
        }
    }

    @Test
    public final void testGetMarket() throws BadRequestException, NotFoundException, IOException {

        long now = NOW;
        postMarket("EURUSD");

        Map<Long, View> views = unrest.getMarket(PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertEquals(1, views.size());
            final View view = views.get(View.composeKey(12, SETTL_DAY));
            assertView("EURUSD", view);
            // Use now beyond expiry.
            now = jdToMillis(EXPIRY_DAY + 1);
            views = unrest.getMarket(PARAMS_EXPIRED, now);
        }

        views = unrest.getMarket(PARAMS_NONE, now);
        assertTrue(views.isEmpty());
    }

    @Test
    public final void testGetMarketContr() throws BadRequestException, NotFoundException,
            IOException {

        long now = NOW;
        postMarket("EURUSD");

        Map<Long, View> views = unrest.getMarket("USDJPY", PARAMS_NONE, now);
        assertTrue(views.isEmpty());

        views = unrest.getMarket("EURUSD", PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertEquals(1, views.size());
            final View view = views.get(View.composeKey(12, SETTL_DAY));
            assertView("EURUSD", view);
            // Use now beyond expiry.
            now = jdToMillis(EXPIRY_DAY + 1);
            views = unrest.getMarket("EURUSD", PARAMS_EXPIRED, now);
        }

        views = unrest.getMarket("EURUSD", PARAMS_NONE, now);
        assertTrue(views.isEmpty());
    }

    @Test
    public final void testGetMarketContrSettl() throws BadRequestException, NotFoundException,
            IOException {

        long now = NOW;
        postMarket("EURUSD");

        try {
            unrest.getMarket("USDJPY", jdToIso(SETTL_DAY), PARAMS_NONE, now);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        try {
            unrest.getMarket("EURUSD", jdToIso(SETTL_DAY + 1), PARAMS_NONE, now);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        View view = unrest.getMarket("EURUSD", jdToIso(SETTL_DAY), PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertView("EURUSD", view);
            // Use now beyond expiry.
            now = jdToMillis(EXPIRY_DAY + 1);
            view = unrest.getMarket("EURUSD", jdToIso(SETTL_DAY), PARAMS_EXPIRED, now);
        }

        try {
            unrest.getMarket("EURUSD", jdToIso(SETTL_DAY), PARAMS_NONE, now);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testPostMarket() throws BadRequestException, NotFoundException, IOException {

        final View view = postMarket("EURUSD");
        assertView("EURUSD", view);
    }

    @Test
    public final void testGetAccnt() throws BadRequestException, NotFoundException, IOException {
        postMarket("EURUSD");
        postOrder("EURUSD", Action.SELL, 12345, 10);
        postOrder("EURUSD", Action.BUY, 12345, 10);
        final AccntStruct out = unrest.getAccnt(EMAIL, PARAMS_NONE, NOW);
        assertOrder("EURUSD", State.TRADE, Action.SELL, 12345, 10, 0, 10, 12345, 10,
                out.orders.get(Long.valueOf(1)));
        assertOrder("EURUSD", State.TRADE, Action.BUY, 12345, 10, 0, 10, 12345, 10,
                out.orders.get(Long.valueOf(2)));
        assertExec("EURUSD", State.TRADE, Action.SELL, 12345, 10, 0, 10, 12345, 10, Role.MAKER,
                out.trades.get(Long.valueOf(3)));
        assertExec("EURUSD", State.TRADE, Action.BUY, 12345, 10, 0, 10, 12345, 10, Role.TAKER,
                out.trades.get(Long.valueOf(4)));
    }

    @Test
    public final void testDeleteOrder() throws BadRequestException, NotFoundException, IOException {
        postMarket("EURUSD");
        postOrder("EURUSD", Action.SELL, 12345, 10);
        putOrder("EURUSD", 1, 0);
        deleteOrder("EURUSD", 1);
        try {
            deleteOrder("EURUSD", 1);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testGetOrder() throws BadRequestException, NotFoundException, IOException {
        postMarket("EURUSD");
        postOrder("EURUSD", Action.SELL, 12345, 10);
        final Map<Long, Order> out = unrest.getOrder(EMAIL, PARAMS_NONE, NOW);
        assertOrder("EURUSD", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0,
                out.get(Long.valueOf(1)));
    }

    @Test
    public final void testGetOrderContr() throws BadRequestException, NotFoundException,
            IOException {
        postMarket("EURUSD");
        postOrder("EURUSD", Action.SELL, 12345, 10);
        final Map<Long, Order> out = unrest.getOrder(EMAIL, "EURUSD", PARAMS_NONE, NOW);
        assertOrder("EURUSD", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0,
                out.get(Long.valueOf(1)));
        assertTrue(unrest.getOrder(EMAIL, "USDJPY", PARAMS_NONE, NOW).isEmpty());
    }

    @Test
    public final void testGetOrderContrSettl() throws BadRequestException, NotFoundException,
            IOException {
        postMarket("EURUSD");
        postOrder("EURUSD", Action.SELL, 12345, 10);
        final Map<Long, Order> out = unrest.getOrder(EMAIL, "EURUSD", jdToIso(SETTL_DAY),
                PARAMS_NONE, NOW);
        assertOrder("EURUSD", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0,
                out.get(Long.valueOf(1)));
        assertTrue(unrest.getOrder(EMAIL, "EURUSD", jdToIso(SETTL_DAY + 1), PARAMS_NONE, NOW)
                .isEmpty());
    }

    @Test
    public final void testGetOrderContrSettlId() throws BadRequestException, NotFoundException,
            IOException {
        postMarket("EURUSD");
        postOrder("EURUSD", Action.SELL, 12345, 10);
        final Order out = unrest.getOrder(EMAIL, "EURUSD", jdToIso(SETTL_DAY), 1, PARAMS_NONE, NOW);
        assertOrder("EURUSD", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0, out);
        try {
            unrest.getOrder(EMAIL, "EURUSD", jdToIso(SETTL_DAY + 1), 2, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testPostOrder() throws BadRequestException, NotFoundException, IOException {
        postMarket("EURUSD");
        final TransStruct out = postOrder("EURUSD", Action.SELL, 12345, 10);
        assertOrder("EURUSD", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
    }

    @Test
    public final void testPutOrder() throws BadRequestException, NotFoundException, IOException {
        postMarket("EURUSD");
        TransStruct out = postOrder("EURUSD", Action.SELL, 12345, 10);
        assertOrder("EURUSD", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
        out = putOrder("EURUSD", 1, 5);
        assertOrder("EURUSD", State.REVISE, Action.SELL, 12345, 5, 5, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
    }

    @Test
    public final void testDeleteTrade() throws BadRequestException, NotFoundException, IOException {
        postMarket("EURUSD");
        postOrder("EURUSD", Action.SELL, 12345, 10);
        postOrder("EURUSD", Action.BUY, 12345, 10);
        final Exec trade = unrest
                .getTrade(EMAIL, "EURUSD", jdToIso(SETTL_DAY), 3, PARAMS_NONE, NOW);
        assertExec("EURUSD", State.TRADE, Action.SELL, 12345, 10, 0, 10, 12345, 10, Role.MAKER,
                trade);
        deleteTrade(EMAIL, "EURUSD", jdToIso(SETTL_DAY), 3);
        try {
            unrest.getTrade(EMAIL, "EURUSD", jdToIso(SETTL_DAY), 3, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testGetTrade() {
    }

    @Test
    public final void testGetTradeContr() {
    }

    @Test
    public final void testGetTradeContrSettl() {
    }

    @Test
    public final void testGetTradeContrSettlId() {
    }

    @Test
    public final void testGetPosn() {
    }

    @Test
    public final void testGetPosnContr() {
    }

    @Test
    public final void testGetPosnContrSettl() {
    }

    @Test
    public final void testGetEndOfDay() {
    }
}

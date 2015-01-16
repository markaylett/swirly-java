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

import org.junit.Test;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
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

    private static View createMarket(Unrest unrest) throws BadRequestException, NotFoundException,
            IOException {
        return unrest.postMarket("EURUSD", jdToIso(SETTL_DAY), jdToIso(FIXING_DAY),
                jdToIso(EXPIRY_DAY), PARAMS_NONE, NOW);
    }

    private static void assertView(String mnem, View view) throws NotFoundException, IOException {
        assertEquals(MockContr.newContr(mnem).getId(), view.getContrId());
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

    private static TransStruct createOrder(Unrest unrest) throws BadRequestException,
            NotFoundException, IOException {
        return unrest.postOrder("mark.aylett@gmail.com", "EURUSD", jdToIso(SETTL_DAY), "foo",
                Action.SELL, 12346, 10, 1, PARAMS_NONE, NOW);
    }

    private static void assertOrder(Order order) {
        assertNotNull(order);
        assertEquals(1, order.getTraderId());
        assertEquals(12, order.getContrId());
        assertEquals(SETTL_DAY, order.getSettlDay());
        assertEquals("foo", order.getRef());
        assertEquals(State.NEW, order.getState());
        assertEquals(Action.SELL, order.getAction());
        assertEquals(12346, order.getTicks());
        assertEquals(10, order.getLots());
        assertEquals(10, order.getResd());
        assertEquals(0, order.getExec());
        assertEquals(0, order.getLastTicks());
        assertEquals(0, order.getLastLots());
        assertEquals(1, order.getMinLots());
        assertEquals(NOW, order.getCreated());
        assertEquals(NOW, order.getModified());
    }

    private static TransStruct createTrade(Unrest unrest) throws BadRequestException,
            NotFoundException, IOException {
        createOrder(unrest);
        return unrest.postOrder("mark.aylett@gmail.com", "EURUSD", jdToIso(SETTL_DAY), "bar",
                Action.BUY, 12344, 5, 1, PARAMS_NONE, NOW);
    }

    @SuppressWarnings("unused")
    private static void assertTrade(Exec trade) {
        assertNotNull(trade);
        assertEquals(1, trade.getTraderId());
        assertEquals(12, trade.getContrId());
        assertEquals(SETTL_DAY, trade.getSettlDay());
        assertEquals("foo", trade.getRef());
        assertEquals(State.NEW, trade.getState());
        assertEquals(Action.SELL, trade.getAction());
        assertEquals(12346, trade.getTicks());
        assertEquals(10, trade.getLots());
        assertEquals(10, trade.getResd());
        assertEquals(0, trade.getExec());
        assertEquals(0, trade.getLastTicks());
        assertEquals(0, trade.getLastLots());
        assertEquals(1, trade.getMinLots());
        assertEquals(1, trade.getMatchId());
        assertEquals(1, trade.getRole());
        assertEquals(1, trade.getCptyId());
        assertEquals(NOW, trade.getCreated());
    }

    @SuppressWarnings("unused")
    private static void assertPosn(Posn posn) {
        assertNotNull(posn);
    }

    @Test
    public final void testGetRec() throws IOException {
        final Unrest unrest = new Unrest(new MockModel());

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
        final Unrest unrest = new Unrest(new MockModel());

        Map<String, Rec> recs = unrest.getRec(RecType.ASSET, PARAMS_NONE, NOW);
        assertAssets(recs);

        recs = unrest.getRec(RecType.CONTR, PARAMS_NONE, NOW);
        assertContrs(recs);

        recs = unrest.getRec(RecType.TRADER, PARAMS_NONE, NOW);
        assertTraders(recs);
    }

    @Test
    public final void testGetRecTypeMnem() throws NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());

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
        } catch (final NotFoundException e) {
        }

        final Trader trader = (Trader) unrest
                .getRec(RecType.TRADER, "MARAYL", PARAMS_NONE, NOW);
        assertTrader("MARAYL", trader);
        try {
            unrest.getRec(RecType.TRADER, "MARAYLx", PARAMS_NONE, NOW);
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testPostTrader() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());

        Trader trader = unrest.postTrader("MARAYL2", "Mark Aylett", "mark.aylett@swirlycloud.com",
                PARAMS_NONE, NOW);
        for (int i = 0; i < 2; ++i) {
            assertNotNull(trader);
            assertEquals("MARAYL2", trader.getMnem());
            assertEquals("Mark Aylett", trader.getDisplay());
            assertEquals("mark.aylett@swirlycloud.com", trader.getEmail());
            trader = (Trader) unrest.getRec(RecType.TRADER, "MARAYL2", PARAMS_NONE, NOW);
        }

        // Duplicate mnemonic.
        try {
            unrest.postTrader("MARAYL", "Mark Aylett", "mark.aylett@swirlycloud.com",
                    PARAMS_NONE, NOW);
        } catch (final BadRequestException e) {
        }

        // Duplicate email.
        try {
            unrest.postTrader("MARAYL3", "Mark Aylett", "mark.aylett@gmail.com", PARAMS_NONE,
                    NOW);
        } catch (final BadRequestException e) {
        }
    }

    @Test
    public final void testGetMarket() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        long now = NOW;

        createMarket(unrest);

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
        final Unrest unrest = new Unrest(new MockModel());
        long now = NOW;

        unrest.postMarket("EURUSD", jdToIso(SETTL_DAY), jdToIso(FIXING_DAY), jdToIso(EXPIRY_DAY),
                PARAMS_NONE, now);

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
        final Unrest unrest = new Unrest(new MockModel());
        long now = NOW;

        unrest.postMarket("EURUSD", jdToIso(SETTL_DAY), jdToIso(FIXING_DAY), jdToIso(EXPIRY_DAY),
                PARAMS_NONE, now);

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
        final Unrest unrest = new Unrest(new MockModel());

        final View view = unrest.postMarket("EURUSD", jdToIso(SETTL_DAY), jdToIso(FIXING_DAY),
                jdToIso(EXPIRY_DAY), PARAMS_NONE, NOW);
        assertView("EURUSD", view);
    }

    @Test
    public final void testGetAccnt() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        createMarket(unrest);
        createOrder(unrest);
        createTrade(unrest);
        AccntStruct out = unrest.getAccnt("mark.aylett@gmail.com", PARAMS_NONE, NOW);
        assertOrder(out.orders.get(Long.valueOf(1)));
        //assertTrade(out.trades.get(Long.valueOf(1)));
    }

    @Test
    public final void testDeleteOrder() {
    }

    @Test
    public final void testGetOrder() {
    }

    @Test
    public final void testGetOrderContr() {
    }

    @Test
    public final void testGetOrderContrSettl() {
    }

    @Test
    public final void testGetOrderContrSettlId() {
    }

    @Test
    public final void testPostOrder() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        createMarket(unrest);
        final TransStruct out = createOrder(unrest);
        assertOrder(out.orders.get(Long.valueOf(1)));
    }

    @Test
    public final void testPutOrder() {
    }

    @Test
    public final void testDeleteTrade() {
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

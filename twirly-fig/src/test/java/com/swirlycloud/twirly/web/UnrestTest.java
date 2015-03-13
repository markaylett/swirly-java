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

import com.swirlycloud.twirly.app.Model;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
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
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.mock.MockAsset;
import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.mock.MockModel;
import com.swirlycloud.twirly.mock.MockTrader;
import com.swirlycloud.twirly.web.Unrest.PosnKey;
import com.swirlycloud.twirly.web.Unrest.RecStruct;
import com.swirlycloud.twirly.web.Unrest.SessStruct;
import com.swirlycloud.twirly.web.Unrest.TransStruct;

public final class UnrestTest {

    private static final String EMAIL = "mark.aylett@gmail.com";
    private static final int TODAY = ymdToJd(2014, 2, 11);
    private static final int EXPIRY_DAY = TODAY + 1;
    private static final int SETTL_DAY = TODAY + 2;

    private static final long NOW = jdToMillis(TODAY);

    private static void assertAsset(Asset expected, Asset actual) {
        assertNotNull(actual);
        assertEquals(expected.getMnem(), actual.getMnem());
        assertEquals(expected.getDisplay(), actual.getDisplay());
        assertEquals(expected.getAssetType(), actual.getAssetType());
    }

    private static void assertAsset(String mnem, Asset actual) {
        assertNotNull(actual);
        assertAsset(MockAsset.newAsset(mnem), actual);
    }

    private static void assertAssets(final Map<String, ? super Asset> assets) {
        MockAsset.selectAsset(new UnaryCallback<Asset>() {
            @Override
            public final void call(Asset arg) {
                assertAsset(arg, (Asset) assets.get(arg.getMnem()));
            }
        });
    }

    private static void assertContr(Contr expected, Contr actual) {
        assertNotNull(actual);
        assertEquals(expected.getAsset(), actual.getAsset());
        assertEquals(expected.getCcy(), actual.getCcy());
        assertEquals(expected.getTickNumer(), actual.getTickNumer());
        assertEquals(expected.getTickDenom(), actual.getTickDenom());
        assertEquals(expected.getLotNumer(), actual.getLotNumer());
        assertEquals(expected.getLotDenom(), actual.getLotDenom());
        assertEquals(expected.getPipDp(), actual.getPipDp());
        assertEquals(expected.getMinLots(), actual.getMinLots());
        assertEquals(expected.getMaxLots(), actual.getMaxLots());
    }

    private static void assertContr(String mnem, Contr actual) {
        assertNotNull(actual);
        assertContr(MockContr.newContr(mnem), actual);
    }

    private static void assertContrs(final Map<String, ? super Contr> contrs) {
        MockContr.selectContr(new UnaryCallback<Contr>() {
            @Override
            public final void call(Contr arg) {
                assertEquals(arg, contrs.get(arg.getMnem()));
            }
        });
    }

    private static void assertMarket(String mnem, String display, String contr, Market actual) {
        assertNotNull(actual);
        assertEquals(mnem, actual.getMnem());
        assertEquals(display, actual.getDisplay());
        assertEquals(contr, actual.getContr());
        assertEquals(SETTL_DAY, actual.getSettlDay());
        assertEquals(EXPIRY_DAY, actual.getExpiryDay());
    }

    private static void assertTrader(Trader expected, Trader actual) {
        assertNotNull(actual);
        assertEquals(expected.getMnem(), actual.getMnem());
        assertEquals(expected.getDisplay(), actual.getDisplay());
        assertEquals(expected.getEmail(), actual.getEmail());
    }

    private static void assertTrader(String mnem, Trader actual) {
        assertNotNull(actual);
        assertTrader(MockTrader.newTrader(mnem), actual);
    }

    private static void assertTraders(final Map<String, ? super Trader> traders) {
        MockTrader.selectTrader(new UnaryCallback<Trader>() {
            @Override
            public final void call(Trader arg) {
                assertEquals(arg, traders.get(arg.getMnem()));
            }
        });
    }

    private static void assertView(String market, String contr, View actual)
            throws NotFoundException, IOException {
        assertNotNull(actual);
        assertEquals(market, actual.getMarket());
        assertEquals(contr, actual.getContr());
        assertEquals(SETTL_DAY, actual.getSettlDay());

        assertEquals(0, actual.getOfferTicks(0));
        assertEquals(0, actual.getOfferLots(0));
        assertEquals(0, actual.getOfferCount(0));

        assertEquals(0, actual.getBidTicks(0));
        assertEquals(0, actual.getBidLots(0));
        assertEquals(0, actual.getOfferCount(0));

        assertEquals(0, actual.getLastTicks());
        assertEquals(0, actual.getLastLots());
        assertEquals(0, actual.getLastTime());
    }

    private static void assertOrder(String market, State state, Action action, long ticks,
            long lots, long resd, long exec, long cost, long lastTicks, long lastLots, Order actual) {
        assertNotNull(actual);
        assertEquals("MARAYL", actual.getTrader());
        assertEquals(market, actual.getMarket());
        assertEquals("", actual.getRef());
        assertEquals(state, actual.getState());
        assertEquals(action, actual.getAction());
        assertEquals(ticks, actual.getTicks());
        assertEquals(lots, actual.getLots());
        assertEquals(resd, actual.getResd());
        assertEquals(exec, actual.getExec());
        assertEquals(cost, actual.getCost());
        assertEquals(lastTicks, actual.getLastTicks());
        assertEquals(lastLots, actual.getLastLots());
        assertEquals(1, actual.getMinLots());
        assertEquals(NOW, actual.getCreated());
        assertEquals(NOW, actual.getModified());
    }

    private static void assertExec(String market, State state, Action action, long ticks,
            long lots, long resd, long exec, long cost, long lastTicks, long lastLots,
            String contr, Role role, Exec actual) {
        assertNotNull(actual);
        assertEquals("MARAYL", actual.getTrader());
        assertEquals(market, actual.getMarket());
        assertEquals("", actual.getRef());
        assertEquals(state, actual.getState());
        assertEquals(action, actual.getAction());
        assertEquals(ticks, actual.getTicks());
        assertEquals(lots, actual.getLots());
        assertEquals(resd, actual.getResd());
        assertEquals(exec, actual.getExec());
        assertEquals(cost, actual.getCost());
        assertEquals(lastTicks, actual.getLastTicks());
        assertEquals(lastLots, actual.getLastLots());
        assertEquals(1, actual.getMinLots());
        assertEquals(contr, actual.getContr());
        assertEquals(SETTL_DAY, actual.getSettlDay());
        assertEquals(role, actual.getRole());
        assertEquals("MARAYL", actual.getCpty());
        assertEquals(NOW, actual.getCreated());
    }

    private static void assertPosn(String market, String contr, long buyCost, long buyLots,
            long sellCost, long sellLots, Posn actual) {
        assertNotNull(actual);
        assertEquals("MARAYL", actual.getTrader());
        assertEquals(contr, actual.getContr());
        assertEquals(SETTL_DAY, actual.getSettlDay());
        assertEquals(buyCost, actual.getBuyCost());
        assertEquals(buyLots, actual.getBuyLots());
        assertEquals(sellCost, actual.getSellCost());
        assertEquals(sellLots, actual.getSellLots());
    }

    private Model model;
    private Unrest unrest;

    private final Trader postTrader(String mnem, String display, String email)
            throws BadRequestException, ServiceUnavailableException, IOException {
        return unrest.postTrader(mnem, display, email, PARAMS_NONE, NOW);
    }

    private final Market postMarket(String mnem, String display, String contr)
            throws BadRequestException, NotFoundException, ServiceUnavailableException, IOException {
        return unrest.postMarket(mnem, display, contr, jdToIso(SETTL_DAY), jdToIso(EXPIRY_DAY),
                PARAMS_NONE, NOW);
    }

    private final void deleteOrder(String market, long id) throws BadRequestException,
            NotFoundException, ServiceUnavailableException, IOException {
        unrest.deleteOrder(EMAIL, market, id, NOW);
    }

    private final TransStruct postOrder(String market, Action action, long ticks, long lots)
            throws BadRequestException, NotFoundException, ServiceUnavailableException, IOException {
        return unrest.postOrder(EMAIL, market, null, action, ticks, lots, 1, PARAMS_NONE, NOW);
    }

    private final TransStruct putOrder(String market, long id, long lots)
            throws BadRequestException, NotFoundException, ServiceUnavailableException, IOException {
        return unrest.putOrder(EMAIL, market, id, lots, PARAMS_NONE, NOW);
    }

    private final void deleteTrade(String email, String market, long id)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        unrest.deleteTrade(EMAIL, market, id, NOW);
    }

    @Before
    public final void setUp() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        model = new MockModel();
        unrest = new Unrest(model);
        postMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD");
    }

    @After
    public final void tearDown() throws Exception {
        model.close();
        unrest = null;
        model = null;
    }

    @Test
    public final void testGetRec() throws IOException {

        // With traders.
        RecStruct st = unrest.getRec(true, PARAMS_NONE, NOW);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertEquals(1, st.markets.size());
        assertMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", st.markets.get("EURUSD.MAR14"));
        assertTraders(st.traders);

        // Without traders.
        st = unrest.getRec(false, PARAMS_NONE, NOW);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertEquals(1, st.markets.size());
        assertMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", st.markets.get("EURUSD.MAR14"));
        assertTrue(st.traders.isEmpty());
    }

    @Test
    public final void testGetRecType() throws IOException {

        Map<String, Rec> recs = unrest.getRec(RecType.ASSET, PARAMS_NONE, NOW);
        assertAssets(recs);

        recs = unrest.getRec(RecType.CONTR, PARAMS_NONE, NOW);
        assertContrs(recs);

        recs = unrest.getRec(RecType.MARKET, PARAMS_NONE, NOW);
        assertEquals(1, recs.size());
        assertMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", (Market) recs.get("EURUSD.MAR14"));

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

        final Market market = (Market) unrest.getRec(RecType.MARKET, "EURUSD.MAR14", PARAMS_NONE,
                NOW);
        assertMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", market);
        try {
            unrest.getRec(RecType.MARKET, "EURUSD.MAR14x", PARAMS_NONE, NOW);
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
    public final void testPostTrader() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {

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
    public final void testGetView() throws BadRequestException, NotFoundException, IOException {

        long now = NOW;

        Map<String, View> views = unrest.getView(PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertEquals(1, views.size());
            final View view = views.get("EURUSD.MAR14");
            assertView("EURUSD.MAR14", "EURUSD", view);
            // Use now beyond expiry.
            now = jdToMillis(EXPIRY_DAY + 1);
            views = unrest.getView(PARAMS_EXPIRED, now);
        }

        views = unrest.getView(PARAMS_NONE, now);
        assertTrue(views.isEmpty());
    }

    @Test
    public final void testGetViewMnem() throws BadRequestException, NotFoundException, IOException {

        long now = NOW;

        try {
            unrest.getView("USDJPY.MAR14", PARAMS_NONE, now);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        View view = unrest.getView("EURUSD.MAR14", PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertView("EURUSD.MAR14", "EURUSD", view);
            // Use now beyond expiry.
            now = jdToMillis(EXPIRY_DAY + 1);
            view = unrest.getView("EURUSD.MAR14", PARAMS_EXPIRED, now);
        }
    }

    @Test
    public final void testPostMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final Market market = postMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD");
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", market);
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD",
                (Market) unrest.getRec(RecType.MARKET, "GBPUSD.MAR14", PARAMS_NONE, NOW));
    }

    @Test
    public final void testGetSess() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        postOrder("EURUSD.MAR14", Action.BUY, 12345, 10);
        final SessStruct out = unrest.getSess(EMAIL, PARAMS_NONE, NOW);
        assertOrder("EURUSD.MAR14", State.TRADE, Action.SELL, 12345, 10, 0, 10, 123450, 12345, 10,
                out.orders.get(Long.valueOf(1)));
        assertOrder("EURUSD.MAR14", State.TRADE, Action.BUY, 12345, 10, 0, 10, 123450, 12345, 10,
                out.orders.get(Long.valueOf(2)));
        assertExec("EURUSD.MAR14", State.TRADE, Action.SELL, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.MAKER, out.trades.get(Long.valueOf(3)));
        assertExec("EURUSD.MAR14", State.TRADE, Action.BUY, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.TAKER, out.trades.get(Long.valueOf(4)));
        assertPosn("EURUSD.MAR14", "EURUSD", 123450, 10, 123450, 10,
                out.posns.get(new PosnKey("EURUSD", SETTL_DAY)));
    }

    @Test
    public final void testDeleteOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        putOrder("EURUSD.MAR14", 1, 0);
        assertNotNull(unrest.getOrder(EMAIL, "EURUSD.MAR14", 1, PARAMS_NONE, NOW));
        deleteOrder("EURUSD.MAR14", 1);
        try {
            unrest.getOrder(EMAIL, "EURUSD.MAR14", 1, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
        try {
            deleteOrder("EURUSD.MAR14", 1);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testGetOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        final Map<Long, Order> out = unrest.getOrder(EMAIL, PARAMS_NONE, NOW);
        assertOrder("EURUSD.MAR14", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.get(Long.valueOf(1)));
    }

    @Test
    public final void testGetOrderMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        final Map<Long, Order> out = unrest.getOrder(EMAIL, "EURUSD.MAR14", PARAMS_NONE, NOW);
        assertOrder("EURUSD.MAR14", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.get(Long.valueOf(1)));
        assertTrue(unrest.getOrder(EMAIL, "USDJPY.MAR14", PARAMS_NONE, NOW).isEmpty());
    }

    @Test
    public final void testGetOrderMarketId() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        final Order out = unrest.getOrder(EMAIL, "EURUSD.MAR14", 1, PARAMS_NONE, NOW);
        assertOrder("EURUSD.MAR14", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0, 0, out);
        try {
            unrest.getOrder(EMAIL, "EURUSD.MAR14", 2, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testPostOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final TransStruct out = postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        assertOrder("EURUSD.MAR14", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
    }

    @Test
    public final void testPutOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        TransStruct out = postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        assertOrder("EURUSD.MAR14", State.NEW, Action.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
        out = putOrder("EURUSD.MAR14", 1, 5);
        assertOrder("EURUSD.MAR14", State.REVISE, Action.SELL, 12345, 5, 5, 0, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
    }

    @Test
    public final void testDeleteTrade() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        postOrder("EURUSD.MAR14", Action.BUY, 12345, 10);
        final Exec trade = unrest.getTrade(EMAIL, "EURUSD.MAR14", 3, PARAMS_NONE, NOW);
        assertExec("EURUSD.MAR14", State.TRADE, Action.SELL, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.MAKER, trade);
        deleteTrade(EMAIL, "EURUSD.MAR14", 3);
        try {
            unrest.getTrade(EMAIL, "EURUSD.MAR14", 3, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testGetTrade() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        postOrder("EURUSD.MAR14", Action.BUY, 12345, 10);
        final Map<Long, Exec> out = unrest.getTrade(EMAIL, PARAMS_NONE, NOW);
        assertExec("EURUSD.MAR14", State.TRADE, Action.SELL, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.MAKER, out.get(Long.valueOf(3)));
        assertExec("EURUSD.MAR14", State.TRADE, Action.BUY, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.TAKER, out.get(Long.valueOf(4)));
    }

    @Test
    public final void testGetTradeMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        postOrder("EURUSD.MAR14", Action.BUY, 12345, 10);
        final Map<Long, Exec> out = unrest.getTrade(EMAIL, "EURUSD.MAR14", PARAMS_NONE, NOW);
        assertExec("EURUSD.MAR14", State.TRADE, Action.SELL, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.MAKER, out.get(Long.valueOf(3)));
        assertExec("EURUSD.MAR14", State.TRADE, Action.BUY, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.TAKER, out.get(Long.valueOf(4)));
        assertTrue(unrest.getTrade(EMAIL, "USDJPY.MAR14", PARAMS_NONE, NOW).isEmpty());
    }

    @Test
    public final void testGetTradeMarketId() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        postOrder("EURUSD.MAR14", Action.BUY, 12345, 10);
        final Map<Long, Exec> out = unrest.getTrade(EMAIL, "EURUSD.MAR14", PARAMS_NONE, NOW);
        assertExec("EURUSD.MAR14", State.TRADE, Action.SELL, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.MAKER, out.get(Long.valueOf(3)));
        assertExec("EURUSD.MAR14", State.TRADE, Action.BUY, 12345, 10, 0, 10, 123450, 12345, 10,
                "EURUSD", Role.TAKER, out.get(Long.valueOf(4)));
        try {
            unrest.getOrder(EMAIL, "EURUSD.MAR14", 3, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
        try {
            unrest.getOrder(EMAIL, "EURUSD.MAR14", 4, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testGetPosn() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        postOrder("EURUSD.MAR14", Action.BUY, 12345, 10);
        final Map<PosnKey, Posn> out = unrest.getPosn(EMAIL, PARAMS_NONE, NOW);
        assertPosn("EURUSD.MAR14", "EURUSD", 123450, 10, 123450, 10,
                out.get(new PosnKey("EURUSD", SETTL_DAY)));
    }

    @Test
    public final void testGetPosnMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("EURUSD.MAR14", Action.SELL, 12345, 10);
        postOrder("EURUSD.MAR14", Action.BUY, 12345, 10);
        final Posn posn = unrest.getPosn(EMAIL, "EURUSD", SETTL_DAY, PARAMS_NONE, NOW);
        assertPosn("EURUSD.MAR14", "EURUSD", 123450, 10, 123450, 10, posn);
        try {
            unrest.getPosn(EMAIL, "USDJPY", SETTL_DAY, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testGetEndOfDay() {
    }
}

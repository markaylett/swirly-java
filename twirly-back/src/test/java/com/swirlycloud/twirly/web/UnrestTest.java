/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_EXPIRED_AND_INTERNAL;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_INTERNAL;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.domain.View;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.mock.MockModel;
import com.swirlycloud.twirly.web.Unrest.RecStruct;
import com.swirlycloud.twirly.web.Unrest.TransStruct;

public final class UnrestTest {

    private static void assertAsset(Asset asset) {
        assertNotNull(asset);
        assertEquals("JPY", asset.getMnem());
        assertEquals("Japan, Yen", asset.getDisplay());
        assertEquals(AssetType.CURRENCY, asset.getAssetType());
    }

    private static void assertAssets(Map<String, ? super Asset> assets) {
        assertNotNull(assets);
        assertEquals(25, assets.size());
        final Asset asset = (Asset) assets.get("JPY");
        assertAsset(asset);
    }

    private static void assertContr(Contr contr) {
        assertNotNull(contr);
        assertEquals("USDJPY", contr.getMnem());
        assertEquals("USDJPY", contr.getDisplay());
        assertEquals(AssetType.CURRENCY, contr.getAssetType());
        assertEquals("USD", contr.getAsset());
        assertEquals("JPY", contr.getCcy());
        assertEquals(1, contr.getTickNumer());
        assertEquals(100, contr.getTickDenom());
        assertEquals(1000000, contr.getLotNumer());
        assertEquals(1, contr.getLotDenom());
        assertEquals(2, contr.getPipDp());
        assertEquals(1, contr.getMinLots());
        assertEquals(10, contr.getMaxLots());
    }

    private static void assertContrs(Map<String, ? super Contr> contrs) {
        assertNotNull(contrs);
        assertEquals(27, contrs.size());
        final Contr contr = (Contr) contrs.get("USDJPY");
        assertContr(contr);
    }

    private static void assertTrader(Trader trader) {
        assertNotNull(trader);
        assertEquals("TOBAYL", trader.getMnem());
        assertEquals("Toby Aylett", trader.getDisplay());
        assertEquals("toby.aylett@gmail.com", trader.getEmail());
    }

    private static void assertTraders(Map<String, ? super Trader> traders) {
        assertNotNull(traders);
        assertEquals(5, traders.size());
        final Trader trader = (Trader) traders.get("TOBAYL");
        assertTrader(trader);
    }

    private static void assertView(View view, long contrId, int settlDay, int fixingDay,
            int expiryDay) {

        assertNotNull(view);

        assertEquals(contrId, view.getContrId());
        assertEquals(settlDay, view.getSettlDay());
        assertEquals(fixingDay, view.getFixingDay());
        assertEquals(expiryDay, view.getExpiryDay());

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

    @Test
    public final void testGetRecWithoutTraders() throws IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final RecStruct st = unrest.getRec(false, PARAMS_NONE, now);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertTrue(st.traders.isEmpty());
    }

    @Test
    public final void testGetRecWithTraders() throws IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final RecStruct st = unrest.getRec(true, PARAMS_NONE, now);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertTraders(st.traders);
    }

    @Test
    public final void testGetRecAsset() throws IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final Map<String, Rec> assets = unrest.getRec(RecType.ASSET, PARAMS_NONE, now);
        assertAssets(assets);
    }

    @Test
    public final void testGetRecContr() throws IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final Map<String, Rec> contrs = unrest.getRec(RecType.CONTR, PARAMS_NONE, now);
        assertContrs(contrs);
    }

    @Test
    public final void testGetRecTrader() throws IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final Map<String, Rec> traders = unrest.getRec(RecType.TRADER, PARAMS_NONE, now);
        assertTraders(traders);
    }

    @Test
    public final void testGetRecAssetMnem() throws NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final Asset asset = (Asset) unrest.getRec(RecType.ASSET, "JPY", PARAMS_NONE, now);
        assertAsset(asset);
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecAssetNotFound() throws NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        unrest.getRec(RecType.ASSET, "JPYx", PARAMS_NONE, now);
    }

    @Test
    public final void testGetRecContrMnem() throws NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final Contr contr = (Contr) unrest.getRec(RecType.CONTR, "USDJPY", PARAMS_NONE, now);
        assertContr(contr);
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecContrNotFound() throws NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        unrest.getRec(RecType.CONTR, "USDJPYx", PARAMS_NONE, now);
    }

    @Test
    public final void testGetRecTraderMnem() throws NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        final Trader trader = (Trader) unrest.getRec(RecType.TRADER, "TOBAYL", PARAMS_NONE, now);
        assertTrader(trader);
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecTraderNotFound() throws NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        unrest.getRec(RecType.TRADER, "TOBAYLx", PARAMS_NONE, now);
    }

    @Test
    public final void testPostTrader() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        Trader trader = unrest.postTrader("MARAYL2", "Mark Aylett", "mark.aylett@swirlycloud.com",
                PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertNotNull(trader);
            assertEquals("MARAYL2", trader.getMnem());
            assertEquals("Mark Aylett", trader.getDisplay());
            assertEquals("mark.aylett@swirlycloud.com", trader.getEmail());
            trader = (Trader) unrest.getRec(RecType.TRADER, "MARAYL2", PARAMS_NONE, now);
        }
    }

    @Test(expected = BadRequestException.class)
    public final void testPostTraderDupMnem() throws BadRequestException, NotFoundException,
            IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        unrest.postTrader("MARAYL", "Mark Aylett", "mark.aylett@swirlycloud.com", PARAMS_NONE, now);
    }

    @Test(expected = BadRequestException.class)
    public final void testPostTraderDupEmail() throws BadRequestException, NotFoundException,
            IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = System.currentTimeMillis();
        unrest.postTrader("MARAYL2", "Mark Aylett", "mark.aylett@gmail.com", PARAMS_NONE, now);
    }

    @Test
    public final void testPostMarket() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        final long now = jdToMillis(ymdToJd(2014, 2, 10));

        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;

        final View view = unrest.postMarket("EURUSD", jdToIso(settlDay), jdToIso(fixingDay),
                jdToIso(expiryDay), PARAMS_INTERNAL, now);
        assertView(view, 12, settlDay, fixingDay, expiryDay);
    }

    @Test
    public final void testGetMarket() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        long now = jdToMillis(ymdToJd(2014, 2, 10));

        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;

        unrest.postMarket("EURUSD", jdToIso(settlDay), jdToIso(fixingDay), jdToIso(expiryDay),
                PARAMS_INTERNAL, now);

        Map<Long, View> views = unrest.getMarket(PARAMS_INTERNAL, now);
        for (int i = 0; i < 2; ++i) {
            assertEquals(1, views.size());
            final View view = views.get(View.composeKey(12, settlDay));
            assertView(view, 12, settlDay, fixingDay, expiryDay);
            // Use now beyond expiry.
            now = jdToMillis(expiryDay + 1);
            views = unrest.getMarket(PARAMS_EXPIRED_AND_INTERNAL, now);
        }

        views = unrest.getMarket(PARAMS_INTERNAL, now);
        assertTrue(views.isEmpty());
    }

    @Test
    public final void testGetMarketMnem() throws BadRequestException, NotFoundException,
            IOException {
        final Unrest unrest = new Unrest(new MockModel());
        long now = jdToMillis(ymdToJd(2014, 2, 10));

        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;

        unrest.postMarket("EURUSD", jdToIso(settlDay), jdToIso(fixingDay), jdToIso(expiryDay),
                PARAMS_INTERNAL, now);

        Map<Long, View> views = unrest.getMarket("USDJPY", PARAMS_INTERNAL, now);
        assertTrue(views.isEmpty());

        views = unrest.getMarket("EURUSD", PARAMS_INTERNAL, now);
        for (int i = 0; i < 2; ++i) {
            assertEquals(1, views.size());
            final View view = views.get(View.composeKey(12, settlDay));
            assertView(view, 12, settlDay, fixingDay, expiryDay);
            // Use now beyond expiry.
            now = jdToMillis(expiryDay + 1);
            views = unrest.getMarket("EURUSD", PARAMS_EXPIRED_AND_INTERNAL, now);
        }

        views = unrest.getMarket("EURUSD", PARAMS_INTERNAL, now);
        assertTrue(views.isEmpty());
    }

    @Test
    public final void testGetMarketMnemSettl() throws BadRequestException, NotFoundException,
            IOException {
        final Unrest unrest = new Unrest(new MockModel());
        long now = jdToMillis(ymdToJd(2014, 2, 10));

        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;

        unrest.postMarket("EURUSD", jdToIso(settlDay), jdToIso(fixingDay), jdToIso(expiryDay),
                PARAMS_INTERNAL, now);

        try {
            unrest.getMarket("USDJPY", jdToIso(settlDay), PARAMS_INTERNAL, now);
            assertTrue(false);
        } catch (final NotFoundException e) {
        }

        try {
            unrest.getMarket("EURUSD", jdToIso(settlDay + 1), PARAMS_INTERNAL, now);
            assertTrue(false);
        } catch (final NotFoundException e) {
        }

        View view = unrest.getMarket("EURUSD", jdToIso(settlDay), PARAMS_INTERNAL, now);
        for (int i = 0; i < 2; ++i) {
            assertView(view, 12, settlDay, fixingDay, expiryDay);
            // Use now beyond expiry.
            now = jdToMillis(expiryDay + 1);
            view = unrest.getMarket("EURUSD", jdToIso(settlDay), PARAMS_EXPIRED_AND_INTERNAL, now);
        }

        try {
            unrest.getMarket("EURUSD", jdToIso(settlDay), PARAMS_INTERNAL, now);
            assertTrue(false);
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testPostOrder() throws BadRequestException, NotFoundException, IOException {
        final Unrest unrest = new Unrest(new MockModel());
        long now = jdToMillis(ymdToJd(2014, 2, 10));

        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;

        unrest.postMarket("EURUSD", jdToIso(settlDay), jdToIso(fixingDay), jdToIso(expiryDay),
                PARAMS_INTERNAL, now);

        final TransStruct out = unrest.postOrder("mark.aylett@gmail.com", "EURUSD",
                jdToIso(settlDay), "", Action.BUY, 12345, 5, 1, PARAMS_INTERNAL, now);
        System.out.println(out.orders);
    }
}

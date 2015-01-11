/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_INTERNAL;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.twirly.util.JsonUtil.parseStartArray;
import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.domain.View;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.mock.MockModel;

public final class RestTest {

    private static <T extends Map<String, Asset>> T parseAssets(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final Asset asset = Asset.parse(p, false);
                out.put(asset.getMnem(), asset);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static <T extends Map<String, Contr>> T parseContrs(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final Contr contr = Contr.parse(p, false);
                out.put(contr.getMnem(), contr);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static <T extends Map<String, Trader>> T parseTraders(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final Trader trader = Trader.parse(p, false);
                out.put(trader.getMnem(), trader);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static <T extends Map<Long, View>> T parseViews(JsonParser p, T out) throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final View view = View.parse(p, false);
                out.put(view.getId(), view);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    public static void assertAsset(Asset asset) {
        assertNotNull(asset);
        assertEquals("JPY", asset.getMnem());
        assertEquals("Japan, Yen", asset.getDisplay());
        assertEquals(AssetType.CURRENCY, asset.getAssetType());
    }

    public static void assertAssets(Map<String, Asset> assets) {
        assertNotNull(assets);
        assertEquals(25, assets.size());
        final Asset asset = assets.get("JPY");
        assertAsset(asset);
    }

    public static void assertContr(Contr contr) {
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

    public static void assertContrs(Map<String, Contr> contrs) {
        assertNotNull(contrs);
        assertEquals(27, contrs.size());
        final Contr contr = contrs.get("USDJPY");
        assertContr(contr);
    }

    public static void assertTrader(Trader trader) {
        assertNotNull(trader);
        assertEquals("TOBAYL", trader.getMnem());
        assertEquals("Toby Aylett", trader.getDisplay());
        assertEquals("toby.aylett@gmail.com", trader.getEmail());
    }

    public static void assertTraders(Map<String, Trader> traders) {
        assertNotNull(traders);
        assertEquals(5, traders.size());
        final Trader trader = traders.get("TOBAYL");
        assertTrader(trader);
    }

    @Test
    public final void testGetRecWithoutTraders() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(false, PARAMS_NONE, now, sb);

        Map<String, Asset> assets = null;
        Map<String, Contr> contrs = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            String name = null;
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_OBJECT:
                    break;
                case KEY_NAME:
                    name = p.getString();
                    break;
                case START_ARRAY:
                    if ("assets".equals(name)) {
                        assertNull(assets);
                        assets = parseAssets(p, new HashMap<String, Asset>());
                    } else if ("contrs".equals(name)) {
                        assertNull(contrs);
                        contrs = parseContrs(p, new HashMap<String, Contr>());
                    } else {
                        assertTrue(false);
                    }
                    break;
                default:
                    assertTrue(false);
                    break;
                }
            }
        }
        assertAssets(assets);
        assertContrs(contrs);
    }

    @Test
    public final void testGetRecWithTraders() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(true, PARAMS_NONE, now, sb);

        Map<String, Asset> assets = null;
        Map<String, Contr> contrs = null;
        Map<String, Trader> traders = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            String name = null;
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_OBJECT:
                    break;
                case KEY_NAME:
                    name = p.getString();
                    break;
                case START_ARRAY:
                    if ("assets".equals(name)) {
                        assertNull(assets);
                        assets = parseAssets(p, new HashMap<String, Asset>());
                    } else if ("contrs".equals(name)) {
                        assertNull(contrs);
                        contrs = parseContrs(p, new HashMap<String, Contr>());
                    } else if ("traders".equals(name)) {
                        assertNull(traders);
                        traders = parseTraders(p, new HashMap<String, Trader>());
                    } else {
                        assertTrue(false);
                    }
                    break;
                default:
                    assertTrue(false);
                    break;
                }
            }
        }
        assertAssets(assets);
        assertContrs(contrs);
        assertTraders(traders);
    }

    @Test
    public final void testGetRecAsset() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.ASSET, PARAMS_NONE, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            final Map<String, Asset> assets = parseAssets(p, new HashMap<String, Asset>());
            assertAssets(assets);
        }
    }

    @Test
    public final void testGetRecContr() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.CONTR, PARAMS_NONE, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            final Map<String, Contr> contrs = parseContrs(p, new HashMap<String, Contr>());
            assertContrs(contrs);
        }
    }

    @Test
    public final void testGetRecTrader() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.TRADER, PARAMS_NONE, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            final Map<String, Trader> traders = parseTraders(p, new HashMap<String, Trader>());
            assertTraders(traders);
        }
    }

    @Test
    public final void testGetRecAssetMnem() throws NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.ASSET, "JPY", PARAMS_NONE, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            final Asset asset = Asset.parse(p, true);
            assertAsset(asset);
        }
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecAssetNotFound() throws NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.ASSET, "JPYx", PARAMS_NONE, now, sb);
    }

    @Test
    public final void testGetRecContrMnem() throws NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.CONTR, "USDJPY", PARAMS_NONE, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            final Contr contr = Contr.parse(p, true);
            assertContr(contr);
        }
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecContrNotFound() throws NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.CONTR, "USDJPYx", PARAMS_NONE, now, sb);
    }

    @Test
    public final void testGetRecTraderMnem() throws NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.TRADER, "TOBAYL", PARAMS_NONE, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            final Trader trader = Trader.parse(p, true);
            assertTrader(trader);
        }
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecTraderNotFound() throws NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.TRADER, "TOBAYLx", PARAMS_NONE, now, sb);
    }

    @Test
    public final void testPostTrader() throws BadRequestException, NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();

        rest.postTrader("MARAYL2", "Mark Aylett", "mark.aylett@swirlycloud.com", PARAMS_NONE, now,
                sb);
        int i = 0;
        do {
            try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
                final Trader trader = Trader.parse(p, true);

                assertNotNull(trader);
                assertEquals("MARAYL2", trader.getMnem());
                assertEquals("Mark Aylett", trader.getDisplay());
                assertEquals("mark.aylett@swirlycloud.com", trader.getEmail());
            }
            sb.setLength(0);
            rest.getRec(RecType.TRADER, "MARAYL2", PARAMS_NONE, now, sb);
        } while (i++ == 0);
    }

    @Test(expected = BadRequestException.class)
    public final void testPostTraderDupMnem() throws BadRequestException, NotFoundException,
            IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.postTrader("MARAYL", "Mark Aylett", "mark.aylett@swirlycloud.com", PARAMS_NONE, now,
                sb);
    }

    @Test(expected = BadRequestException.class)
    public final void testPostTraderDupEmail() throws BadRequestException, NotFoundException,
            IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.postTrader("MARAYL2", "Mark Aylett", "mark.aylett@gmail.com", PARAMS_NONE, now, sb);
    }

    @Test
    public final void testPostMarket() throws BadRequestException, NotFoundException, IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = jdToMillis(ymdToJd(2014, 2, 11));
        final StringBuilder sb = new StringBuilder();

        final int settlDay = ymdToJd(2014, 2, 14);
        final int fixingDay = settlDay - 2;
        final int expiryDay = settlDay - 3;

        rest.postMarket("EURUSD", jdToIso(settlDay), jdToIso(fixingDay), jdToIso(expiryDay),
                PARAMS_INTERNAL, now, sb);
        int i = 0;
        do {
            try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
                final View view = View.parse(p, true);

                assertEquals(12, view.getContrId());
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
            sb.setLength(0);
            rest.getMarket("EURUSD", jdToIso(settlDay), PARAMS_INTERNAL, now, sb);
        } while (i++ == 0);
    }

    @Test
    public final void testGetMarket() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getMarket(PARAMS_INTERNAL, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            final Map<Long, View> views = parseViews(p, new HashMap<Long, View>());
            assertTrue(views.isEmpty());
        }
    }
}

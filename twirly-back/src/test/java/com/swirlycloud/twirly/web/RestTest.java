/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
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
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.mock.MockModel;
import com.swirlycloud.twirly.util.Params;

public final class RestTest {
    private static final Params NO_PARAMS = new Params() {
        @Override
        public final <T> T getParam(String name, Class<T> clazz) {
            return null;
        }
    };

    private static <T extends Map<String, Asset>> T parseAssets(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final Asset asset = Asset.parse(p);
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
                final Contr contr = Contr.parse(p);
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
                final Trader trader = Trader.parse(p);
                out.put(trader.getMnem(), trader);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static Trader parseTrader(Reader reader) throws IOException {
        Trader trader = null;
        try (JsonParser p = Json.createParser(reader)) {
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
                case END_OBJECT:
                    assertTrue(false);
                    break;
                case KEY_NAME:
                    assertTrue(false);
                    break;
                case START_ARRAY:
                    assertTrue(false);
                    break;
                case START_OBJECT:
                    assertNull(trader);
                    trader = Trader.parse(p);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
                    assertTrue(false);
                    break;
                }
            }
        }
        return trader;
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
        rest.getRec(false, NO_PARAMS, now, sb);

        Map<String, Asset> assets = null;
        Map<String, Contr> contrs = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            String name = null;
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
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
                case START_OBJECT:
                    assertTrue(name == null);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
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
        rest.getRec(true, NO_PARAMS, now, sb);

        Map<String, Asset> assets = null;
        Map<String, Contr> contrs = null;
        Map<String, Trader> traders = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            String name = null;
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
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
                case START_OBJECT:
                    assertTrue(name == null);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
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
        rest.getRec(RecType.ASSET, NO_PARAMS, now, sb);

        Map<String, Asset> assets = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
                case END_OBJECT:
                    assertTrue(false);
                    break;
                case KEY_NAME:
                    assertTrue(false);
                    break;
                case START_ARRAY:
                    assertNull(assets);
                    assets = parseAssets(p, new HashMap<String, Asset>());
                    break;
                case START_OBJECT:
                    assertTrue(false);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
                    assertTrue(false);
                    break;
                }
            }
        }
        assertAssets(assets);
    }

    @Test
    public final void testGetRecContr() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.CONTR, NO_PARAMS, now, sb);

        Map<String, Contr> contrs = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
                case END_OBJECT:
                    assertTrue(false);
                    break;
                case KEY_NAME:
                    assertTrue(false);
                    break;
                case START_ARRAY:
                    assertNull(contrs);
                    contrs = parseContrs(p, new HashMap<String, Contr>());
                    break;
                case START_OBJECT:
                    assertTrue(false);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
                    assertTrue(false);
                    break;
                }
            }
        }
        assertContrs(contrs);
    }

    @Test
    public final void testGetRecTrader() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.TRADER, NO_PARAMS, now, sb);

        Map<String, Trader> traders = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
                case END_OBJECT:
                    assertTrue(false);
                    break;
                case KEY_NAME:
                    assertTrue(false);
                    break;
                case START_ARRAY:
                    assertNull(traders);
                    traders = parseTraders(p, new HashMap<String, Trader>());
                    break;
                case START_OBJECT:
                    assertTrue(false);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
                    assertTrue(false);
                    break;
                }
            }
        }
        assertTraders(traders);
    }

    @Test
    public final void testGetRecAssetMnem() throws IOException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.ASSET, "JPY", NO_PARAMS, now, sb);

        Asset asset = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
                case END_OBJECT:
                    assertTrue(false);
                    break;
                case KEY_NAME:
                    assertTrue(false);
                    break;
                case START_ARRAY:
                    assertTrue(false);
                    break;
                case START_OBJECT:
                    assertNull(asset);
                    asset = Asset.parse(p);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
                    assertTrue(false);
                    break;
                }
            }
        }
        assertAsset(asset);
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecAssetNotFound() throws IOException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.ASSET, "JPYx", NO_PARAMS, now, sb);
    }

    @Test
    public final void testGetRecContrMnem() throws IOException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.CONTR, "USDJPY", NO_PARAMS, now, sb);

        Contr contr = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            while (p.hasNext()) {
                final Event event = p.next();
                switch (event) {
                case END_ARRAY:
                    assertTrue(false);
                    break;
                case END_OBJECT:
                    assertTrue(false);
                    break;
                case KEY_NAME:
                    assertTrue(false);
                    break;
                case START_ARRAY:
                    assertTrue(false);
                    break;
                case START_OBJECT:
                    assertNull(contr);
                    contr = Contr.parse(p);
                    break;
                case VALUE_FALSE:
                    assertTrue(false);
                    break;
                case VALUE_NULL:
                    assertTrue(false);
                    break;
                case VALUE_NUMBER:
                    assertTrue(false);
                    break;
                case VALUE_STRING:
                    assertTrue(false);
                    break;
                case VALUE_TRUE:
                    assertTrue(false);
                    break;
                }
            }
        }
        assertContr(contr);
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecContrNotFound() throws IOException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.CONTR, "USDJPYx", NO_PARAMS, now, sb);
    }

    @Test
    public final void testGetRecTraderMnem() throws IOException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.TRADER, "TOBAYL", NO_PARAMS, now, sb);

        assertTrader(parseTrader(new StringReader(sb.toString())));
    }

    @Test(expected = NotFoundException.class)
    public final void testGetRecTraderNotFound() throws IOException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.getRec(RecType.TRADER, "TOBAYLx", NO_PARAMS, now, sb);
    }

    @Test
    public final void testPostTrader() throws IOException, BadRequestException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.postTrader("MARAYL2", "Mark Aylett", "mark.aylett@swirlycloud.com", now, sb);

        Trader trader = parseTrader(new StringReader(sb.toString()));
        int i = 0;
        do {
            assertNotNull(trader);
            assertEquals("MARAYL2", trader.getMnem());
            assertEquals("Mark Aylett", trader.getDisplay());
            assertEquals("mark.aylett@swirlycloud.com", trader.getEmail());
            sb.setLength(0);
            rest.getRec(RecType.TRADER, "MARAYL2", NO_PARAMS, now, sb);
        } while (i++ == 0);
    }

    @Test(expected = BadRequestException.class)
    public final void testPostTraderDupMnem() throws IOException, BadRequestException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.postTrader("MARAYL", "Mark Aylett", "mark.aylett@swirlycloud.com", now, sb);
    }

    @Test(expected = BadRequestException.class)
    public final void testPostTraderDupEmail() throws IOException, BadRequestException, NotFoundException {
        final Rest rest = new Rest(new MockModel());
        final long now = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        rest.postTrader("MARAYL2", "Mark Aylett", "mark.aylett@gmail.com", now, sb);
    }
}
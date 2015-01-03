/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static org.junit.Assert.*;

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
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.function.UnaryFunction;
import com.swirlycloud.twirly.mock.MockModel;

public final class RestTest {
    private static Asset parseAsset(JsonParser p) throws IOException {
        long id = 0;
        String mnem = null;
        String display = null;
        AssetType type = null;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Asset(id, mnem, display, type);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    display = p.getString();
                } else if ("type".equals(name)) {
                    type = AssetType.valueOf(p.getString());
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    private static <T extends Map<String, Asset>> T parseAssets(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final Asset asset = parseAsset(p);
                out.put(asset.getMnem(), asset);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static Contr parseContr(JsonParser p) throws IOException {
        long id = 0;
        String mnem = null;
        String display = null;
        AssetType assetType = null;
        String asset = null;
        String ccy = null;
        int tickNumer = 0;
        int tickDenom = 0;
        int lotNumer = 0;
        int lotDenom = 0;
        int pipDp = 0;
        long minLots = 0;
        long maxLots = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Contr(id, mnem, display, assetType, asset, ccy, tickNumer, tickDenom,
                        lotNumer, lotDenom, pipDp, minLots, maxLots);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else if ("tickNumer".equals(name)) {
                    tickNumer = p.getInt();
                } else if ("tickDenom".equals(name)) {
                    tickDenom = p.getInt();
                } else if ("lotNumer".equals(name)) {
                    lotNumer = p.getInt();
                } else if ("lotDenom".equals(name)) {
                    lotDenom = p.getInt();
                } else if ("pipDp".equals(name)) {
                    pipDp = p.getInt();
                } else if ("minLots".equals(name)) {
                    minLots = p.getLong();
                } else if ("maxLots".equals(name)) {
                    maxLots = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    display = p.getString();
                } else if ("assetType".equals(name)) {
                    assetType = AssetType.valueOf(p.getString());
                } else if ("asset".equals(name)) {
                    asset = p.getString();
                } else if ("ccy".equals(name)) {
                    ccy = p.getString();
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    private static <T extends Map<String, Contr>> T parseContrs(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final Contr contr = parseContr(p);
                out.put(contr.getMnem(), contr);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static Trader parseTrader(JsonParser p) throws IOException {
        long id = 0;
        String mnem = null;
        String display = null;
        String email = null;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Trader(id, mnem, display, email);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    display = p.getString();
                } else if ("email".equals(name)) {
                    email = p.getString();
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    private static <T extends Map<String, Trader>> T parseTraders(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                final Trader trader = parseTrader(p);
                out.put(trader.getMnem(), trader);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    @Test
    public final void testGetRecNotAdmin() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final StringBuilder sb = new StringBuilder();
        rest.getRec(false, new UnaryFunction<String, String>() {
            @Override
            public final String call(String arg) {
                return null;
            }
        }, sb);

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
                        assets = parseAssets(p, new HashMap<String, Asset>());
                    } else if ("contrs".equals(name)) {
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
        assertNotNull(assets);
        assertEquals(25, assets.size());
        final Asset asset = assets.get("JPY");
        assertEquals("JPY", asset.getMnem());
        assertEquals("Japan, Yen", asset.getDisplay());
        assertEquals(AssetType.CURRENCY, asset.getAssetType());

        assertNotNull(contrs);
        assertEquals(27, contrs.size());
        final Contr contr = contrs.get("USDJPY");
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

    @Test
    public final void testGetRecAdmin() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final StringBuilder sb = new StringBuilder();
        rest.getRec(true, new UnaryFunction<String, String>() {
            @Override
            public final String call(String arg) {
                return null;
            }
        }, sb);

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
                        assets = parseAssets(p, new HashMap<String, Asset>());
                    } else if ("contrs".equals(name)) {
                        contrs = parseContrs(p, new HashMap<String, Contr>());
                    } else if ("traders".equals(name)) {
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
        assertNotNull(assets);
        assertEquals(25, assets.size());
        final Asset asset = assets.get("JPY");
        assertEquals("JPY", asset.getMnem());
        assertEquals("Japan, Yen", asset.getDisplay());
        assertEquals(AssetType.CURRENCY, asset.getAssetType());

        assertNotNull(contrs);
        assertEquals(27, contrs.size());
        final Contr contr = contrs.get("USDJPY");
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

        assertNotNull(traders);
        assertEquals(5, traders.size());
        final Trader trader = traders.get("TOBAYL");
        assertEquals("TOBAYL", trader.getMnem());
        assertEquals("Toby Aylett", trader.getDisplay());
        assertEquals("toby.aylett@gmail.com", trader.getEmail());
    }
}

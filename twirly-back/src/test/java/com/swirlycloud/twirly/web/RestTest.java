/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.domain.Contr;
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

    private static <T extends Collection<Asset>> T parseAssets(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                out.add(parseAsset(p));
                break;
            case VALUE_NULL:
                out.add(null);
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

    private static <T extends Collection<Contr>> T parseContrs(JsonParser p, T out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return out;
            case START_OBJECT:
                out.add(parseContr(p));
                break;
            case VALUE_NULL:
                out.add(null);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    @Test
    public final void testGetRec() throws IOException {
        final Rest rest = new Rest(new MockModel());
        final StringBuilder sb = new StringBuilder();
        rest.getRec(false, new UnaryFunction<String, String>() {
            @Override
            public final String call(String arg) {
                return null;
            }
        }, sb);

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
                        System.out.print(parseAssets(p, new ArrayList<Asset>()));
                    } else if ("contrs".equals(name)) {
                        System.out.print(parseContrs(p, new ArrayList<Contr>()));
                    } else {
                        assertTrue(false);
                    }
                    break;
                case START_OBJECT:
                    if (name == null) {
                    } else if ("asset".equals(name)) {
                        System.out.print(parseAsset(p));
                    } else if ("contr".equals(name)) {
                        System.out.print(parseContr(p));
                    } else {
                        assertTrue(false);
                    }
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
    }
}

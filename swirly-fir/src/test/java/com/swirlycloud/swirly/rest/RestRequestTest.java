/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.util.JsonUtil.parseStartObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.Test;

import com.swirlycloud.swirly.domain.Side;

public final class RestRequestTest {
    private static final RestRequest parse(String s) throws IOException {
        final RestRequest r = new RestRequest();
        try (JsonParser p = Json.createParser(new StringReader(s))) {
            parseStartObject(p);
            r.parse(p);
        }
        return r;
    }

    @Test
    public final void testNull() throws IOException {
        final RestRequest r = parse("{\"mnem\":null}");
        assertEquals(0, r.getFields());
        assertNull(r.getMnem());
    }

    @Test
    public final void testLong() throws IOException {
        final RestRequest r = parse("{\"ticks\":12345}");
        assertEquals(RestRequest.TICKS, r.getFields());
        assertEquals(12345, r.getTicks());
    }

    @Test
    public final void testString() throws IOException {
        final RestRequest r = parse("{\"contr\":\"EURUSD\"}");
        assertEquals(RestRequest.CONTR, r.getFields());
        assertEquals("EURUSD", r.getContr());
    }

    @Test
    public final void testAction() throws IOException {
        final RestRequest r = parse("{\"side\":\"SELL\"}");
        assertEquals(RestRequest.SIDE, r.getFields());
        assertEquals(Side.SELL, r.getSide());
    }

    @Test
    public final void testMulti() throws IOException {
        final RestRequest r = parse("{\"contr\":\"EURUSD\",\"settlDate\":20140314}");
        assertEquals(RestRequest.CONTR | RestRequest.SETTL_DATE, r.getFields());
        assertEquals("EURUSD", r.getContr());
        assertEquals(20140314, r.getSettlDate());
    }

    public final void testDuplicate() throws IOException {
        final RestRequest r = parse("{\"trader\":\"MARAYL1\",\"trader\":\"MARAYL2\"}");
        assertEquals(RestRequest.TRADER, r.getFields());
        // First value is overwritten with second.
        assertEquals("MARAYL2", r.getTrader());
    }

    @Test(expected = IOException.class)
    public final void testBadField() throws IOException {
        parse("{\"foo\":null}");
    }

    @Test(expected = IOException.class)
    public final void testBadType() throws IOException {
        parse("{\"ticks\":\"101\"}");
    }

    @Test(expected = IOException.class)
    public final void testBadObject() throws IOException {
        parse("[{\"ticks\":101}]");
    }
}

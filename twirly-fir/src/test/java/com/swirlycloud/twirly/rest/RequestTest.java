/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Side;

public final class RequestTest {
    private static final Request parse(String s) throws IOException {
        final Request r = new Request();
        try (JsonParser p = Json.createParser(new StringReader(s))) {
            parseStartObject(p);
            r.parse(p);
        }
        return r;
    }

    @Test
    public final void testNull() throws IOException {
        final Request r = parse("{\"mnem\":null}");
        assertEquals(Request.MNEM, r.getFields());
        assertNull(r.getMnem());
    }

    @Test
    public final void testLong() throws IOException {
        final Request r = parse("{\"ticks\":12345}");
        assertEquals(Request.TICKS, r.getFields());
        assertEquals(12345, r.getTicks());
    }

    @Test
    public final void testString() throws IOException {
        final Request r = parse("{\"contr\":\"EURUSD\"}");
        assertEquals(Request.CONTR, r.getFields());
        assertEquals("EURUSD", r.getContr());
    }

    @Test
    public final void testAction() throws IOException {
        final Request r = parse("{\"side\":\"SELL\"}");
        assertEquals(Request.SIDE, r.getFields());
        assertEquals(Side.SELL, r.getSide());
    }

    @Test
    public final void testMulti() throws IOException {
        final Request r = parse("{\"contr\":\"EURUSD\",\"settlDate\":20140314}");
        assertEquals(Request.CONTR | Request.SETTL_DATE, r.getFields());
        assertEquals("EURUSD", r.getContr());
        assertEquals(20140314, r.getSettlDate());
    }

    public final void testDuplicate() throws IOException {
        final Request r = parse("{\"trader\":\"MARAYL1\",\"trader\":\"MARAYL2\"}");
        assertEquals(Request.TRADER, r.getFields());
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Action;

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
    public final void testLong() throws IOException {
        final Request r = parse("{\"id\":101}");
        assertEquals(Request.ID, r.getFields());
        assertEquals(101, r.getId());
    }

    @Test
    public final void testString() throws IOException {
        final Request r = parse("{\"contr\":\"EURUSD\"}");
        assertEquals(Request.CONTR, r.getFields());
        assertEquals("EURUSD", r.getContr());
    }

    @Test
    public final void testAction() throws IOException {
        final Request r = parse("{\"action\":\"SELL\"}");
        assertEquals(Request.ACTION, r.getFields());
        assertEquals(Action.SELL, r.getAction());
    }

    @Test
    public final void testMulti() throws IOException {
        final Request r = parse("{\"contr\":\"EURUSD\",\"settlDate\":20140314}");
        assertEquals(Request.CONTR | Request.SETTL_DATE, r.getFields());
        assertEquals("EURUSD", r.getContr());
        assertEquals(20140314, r.getSettlDate());
    }

    @Test(expected = IOException.class)
    public final void testDuplicate() throws IOException {
        parse("{\"trader\":\"MARAYL\",\"trader\":\"MARAYL\"}");
    }

    @Test(expected = IOException.class)
    public final void testBadType() throws IOException {
        parse("{\"ticks\":\"101\"}");
    }

    @Test(expected = IOException.class)
    public final void testArray() throws IOException {
        parse("[{\"ticks\":101}]");
    }
}

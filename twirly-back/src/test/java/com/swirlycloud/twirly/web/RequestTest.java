/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.exception.BadRequestException;

public final class RequestTest {
    private static final Request parse(String s) throws BadRequestException {
        final Request r = new Request();
        try (JsonParser p = Json.createParser(new StringReader(s))) {
            r.parse(p, true);
        }
        return r;
    }

    @Test
    public final void testLong() throws BadRequestException {
        final Request r = parse("{\"id\":101}");
        assertEquals(Request.ID, r.getFields());
        assertEquals(101, r.getId());
    }

    @Test
    public final void testString() throws BadRequestException {
        final Request r = parse("{\"contr\":\"EURUSD\"}");
        assertEquals(Request.CONTR, r.getFields());
        assertEquals("EURUSD", r.getContr());
    }

    @Test
    public final void testAction() throws BadRequestException {
        final Request r = parse("{\"action\":\"SELL\"}");
        assertEquals(Request.ACTION, r.getFields());
        assertEquals(Action.SELL, r.getAction());
    }

    @Test
    public final void testMulti() throws BadRequestException {
        final Request r = parse("{\"contr\":\"EURUSD\",\"settlDate\":20140314}");
        assertEquals(Request.CONTR | Request.SETTL_DATE, r.getFields());
        assertEquals("EURUSD", r.getContr());
        assertEquals(20140314, r.getSettlDate());
    }

    @Test(expected = BadRequestException.class)
    public final void testDuplicate() throws BadRequestException {
        parse("{\"trader\":\"MARAYL\",\"trader\":\"MARAYL\"}");
    }

    @Test(expected = BadRequestException.class)
    public final void testBadType() throws BadRequestException {
        parse("{\"ticks\":\"101\"}");
    }

    @Test(expected = BadRequestException.class)
    public final void testArray() throws BadRequestException {
        parse("[{\"ticks\":101}]");
    }
}

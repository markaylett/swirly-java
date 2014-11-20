/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.junit.Assert.*;

import org.doobry.domain.Action;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public final class RequestTest {
    private static final Request parse(String s) throws ParseException {
        final JSONParser p = new JSONParser();
        final Request r = new Request();
        p.parse(s, r);
        return r;
    }

    @Test
    public final void testLong() throws ParseException {
        final Request r = parse("{\"id\":101}");
        assertTrue(r.isValid());
        assertEquals(Request.ID, r.getFields());
        assertEquals(101, r.getId());
    }

    @Test
    public final void testString() throws ParseException {
        final Request r = parse("{\"contr\":\"EURUSD\"}");
        assertTrue(r.isValid());
        assertEquals(Request.CONTR, r.getFields());
        assertEquals("EURUSD", r.getContr());
    }

    @Test
    public final void testAction() throws ParseException {
        final Request r = parse("{\"action\":\"SELL\"}");
        assertTrue(r.isValid());
        assertEquals(Request.ACTION, r.getFields());
        assertEquals(Action.SELL, r.getAction());
    }

    @Test
    public final void testMulti() throws ParseException {
        final Request r = parse("{\"contr\":\"EURUSD\",\"settlDate\":20140314}");
        assertTrue(r.isValid());
        assertEquals(Request.CONTR | Request.SETTL_DATE, r.getFields());
        assertEquals("EURUSD", r.getContr());
        assertEquals(20140314, r.getSettlDate());
    }

    @Test
    public final void testDuplicate() throws ParseException {
        final Request r = parse("{\"user\":\"MARAYL\",\"user\":\"MARAYL\"}");
        assertTrue(!r.isValid());
    }

    @Test
    public final void testBadType() throws ParseException {
        final Request r = parse("{\"ticks\":\"101\"}");
        assertTrue(!r.isValid());
    }

    @Test
    public final void testArray() throws ParseException {
        final Request r = parse("[{\"ticks\":101}]");
        assertTrue(!r.isValid());
    }
}

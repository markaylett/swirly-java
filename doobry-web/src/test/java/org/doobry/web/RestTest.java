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

public final class RestTest {
    private static final Rest parse(String s) throws ParseException {
        final JSONParser p = new JSONParser();
        final Rest r = new Rest();
        p.parse(s, r);
        return r;
    }

    @Test
    public final void testLong() throws ParseException {
        final Rest r = parse("{\"id\":101}");
        assertTrue(r.isValid());
        assertEquals(Rest.ID, r.getFields());
        assertEquals(101, r.getId());
    }

    @Test
    public final void testString() throws ParseException {
        final Rest r = parse("{\"user\":\"WRAMIREZ\"}");
        assertTrue(r.isValid());
        assertEquals(Rest.ACCNT, r.getFields());
        assertEquals("WRAMIREZ", r.getAccnt());
    }

    @Test
    public final void testAction() throws ParseException {
        final Rest r = parse("{\"action\":\"SELL\"}");
        assertTrue(r.isValid());
        assertEquals(Rest.ACTION, r.getFields());
        assertEquals(Action.SELL, r.getAction());
    }

    @Test
    public final void testMulti() throws ParseException {
        final Rest r = parse("{\"contr\":\"EURUSD\",\"settl_date\":20140314}");
        assertTrue(r.isValid());
        assertEquals(Rest.CONTR | Rest.SETTL_DATE, r.getFields());
        assertEquals("EURUSD", r.getContr());
        assertEquals(20140314, r.getSettlDate());
    }

    @Test
    public final void testDuplicate() throws ParseException {
        final Rest r = parse("{\"user\":\"WRAMIREZ\",\"user\":\"WRAMIREZ\"}");
        assertTrue(!r.isValid());
    }

    @Test
    public final void testWrongType() throws ParseException {
        final Rest r = parse("{\"ticks\":\"101\"}");
        assertTrue(!r.isValid());
    }

    @Test
    public final void testArray() throws ParseException {
        final Rest r = parse("[{\"ticks\":101}]");
        assertTrue(!r.isValid());
    }
}

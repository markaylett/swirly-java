/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static org.junit.Assert.*;

import org.junit.Test;

import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.engine.Cache;
import com.swirlycloud.engine.Model;
import com.swirlycloud.mock.MockModel;

public final class CacheTest {
    private final Model model = new MockModel();

    @Test
    public final void testEmpty() {
        final Cache c = new Cache(257);
        assertTrue(c.isEmptyRec(Kind.ASSET));
        assertTrue(c.isEmptyRec(Kind.CONTR));
        assertTrue(c.isEmptyRec(Kind.USER));
        assertNull(c.getFirstRec(Kind.ASSET));
        assertNull(c.getFirstRec(Kind.CONTR));
        assertNull(c.getFirstRec(Kind.USER));
    }

    @Test
    public final void testAsset() {
        final Cache c = new Cache(257);
        c.insertRecList(Kind.ASSET, model.getRecList(Kind.ASSET));
        assertFalse(c.isEmptyRec(Kind.ASSET));
        assertEquals("AUD", ((Rec) c.getFirstRec(Kind.ASSET)).getMnem());
        assertEquals("CAD", ((Rec) c.getFirstRec(Kind.ASSET).slNext()).getMnem());
        assertEquals("CHF", c.findRec(Kind.ASSET, "CHF").getMnem());
        assertEquals("CZK", c.findRec(Kind.ASSET, 4).getMnem());
    }

    @Test
    public final void testContr() {
        final Cache c = new Cache(257);
        c.insertRecList(Kind.CONTR, model.getRecList(Kind.CONTR));
        assertFalse(c.isEmptyRec(Kind.CONTR));
        assertEquals("AUDUSD", ((Rec) c.getFirstRec(Kind.CONTR)).getMnem());
        assertEquals("EURCHF", ((Rec) c.getFirstRec(Kind.CONTR).slNext()).getMnem());
        assertEquals("EURCZK", c.findRec(Kind.CONTR, "EURCZK").getMnem());
        assertEquals("EURDKK", c.findRec(Kind.CONTR, 4).getMnem());
    }

    @Test
    public final void testUser() {
        final Cache c = new Cache(257);
        c.insertRecList(Kind.USER, model.getRecList(Kind.USER));
        assertFalse(c.isEmptyRec(Kind.USER));
        assertEquals("EMIAYL", ((Rec) c.getFirstRec(Kind.USER)).getMnem());
        assertEquals("GOSAYL", ((Rec) c.getFirstRec(Kind.USER).slNext()).getMnem());
        assertEquals("MARAYL", c.findRec(Kind.USER, "MARAYL").getMnem());
        assertEquals("TOBAYL", c.findRec(Kind.USER, 3).getMnem());
    }
}
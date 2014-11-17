/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import static org.junit.Assert.*;

import org.doobry.domain.Rec;
import org.doobry.domain.Kind;
import org.doobry.mock.MockModel;
import org.junit.Test;

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
        c.insertList(Kind.ASSET, model.selectRec(Kind.ASSET));
        assertFalse(c.isEmptyRec(Kind.ASSET));
        assertEquals("AUD", ((Rec) c.getFirstRec(Kind.ASSET)).getMnem());
        assertEquals("CAD", ((Rec) c.getFirstRec(Kind.ASSET).slNext()).getMnem());
        assertEquals("CHF", c.findRec(Kind.ASSET, "CHF").getMnem());
        assertEquals("CZK", c.findRec(Kind.ASSET, 4).getMnem());
    }

    @Test
    public final void testContr() {
        final Cache c = new Cache(257);
        c.insertList(Kind.CONTR, model.selectRec(Kind.CONTR));
        assertFalse(c.isEmptyRec(Kind.CONTR));
        assertEquals("AUDUSD", ((Rec) c.getFirstRec(Kind.CONTR)).getMnem());
        assertEquals("EURCHF", ((Rec) c.getFirstRec(Kind.CONTR).slNext()).getMnem());
        assertEquals("EURCZK", c.findRec(Kind.CONTR, "EURCZK").getMnem());
        assertEquals("EURDKK", c.findRec(Kind.CONTR, 4).getMnem());
    }

    @Test
    public final void testUser() {
        final Cache c = new Cache(257);
        c.insertList(Kind.USER, model.selectRec(Kind.USER));
        assertFalse(c.isEmptyRec(Kind.USER));
        assertEquals("BJONES", ((Rec) c.getFirstRec(Kind.USER)).getMnem());
        assertEquals("EEDWARDS", ((Rec) c.getFirstRec(Kind.USER).slNext()).getMnem());
        assertEquals("GWILSON", c.findRec(Kind.USER, "GWILSON").getMnem());
        assertEquals("JTHOMAS", c.findRec(Kind.USER, 4).getMnem());
    }
}

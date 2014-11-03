/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import static org.junit.Assert.*;

import org.doobry.domain.Model;
import org.doobry.domain.Rec;
import org.doobry.domain.RecType;
import org.doobry.mock.MockModel;
import org.junit.Test;

public final class CacheTest {
    private final Model model = new MockModel();

    @Test
    public final void testEmpty() {
        final Cache c = new Cache(257);
        assertTrue(c.isEmptyRec(RecType.ASSET));
        assertTrue(c.isEmptyRec(RecType.CONTR));
        assertTrue(c.isEmptyRec(RecType.USER));
        assertNull(c.getFirstRec(RecType.ASSET));
        assertNull(c.getFirstRec(RecType.CONTR));
        assertNull(c.getFirstRec(RecType.USER));
    }

    @Test
    public final void testAsset() {
        final Cache c = new Cache(257);
        c.insertList(RecType.ASSET, model.readRec(RecType.ASSET));
        assertFalse(c.isEmptyRec(RecType.ASSET));
        assertEquals("AUD", ((Rec) c.getFirstRec(RecType.ASSET)).getMnem());
        assertEquals("CAD", ((Rec) c.getFirstRec(RecType.ASSET).slNext()).getMnem());
        assertEquals("CHF", c.findRecMnem(RecType.ASSET, "CHF").getMnem());
        assertEquals("CZK", c.findRecId(RecType.ASSET, 4).getMnem());
    }

    @Test
    public final void testContr() {
        final Cache c = new Cache(257);
        c.insertList(RecType.CONTR, model.readRec(RecType.CONTR));
        assertFalse(c.isEmptyRec(RecType.CONTR));
        assertEquals("AUDUSD", ((Rec) c.getFirstRec(RecType.CONTR)).getMnem());
        assertEquals("EURCHF", ((Rec) c.getFirstRec(RecType.CONTR).slNext()).getMnem());
        assertEquals("EURCZK", c.findRecMnem(RecType.CONTR, "EURCZK").getMnem());
        assertEquals("EURDKK", c.findRecId(RecType.CONTR, 4).getMnem());
    }

    @Test
    public final void testUser() {
        final Cache c = new Cache(257);
        c.insertList(RecType.USER, model.readRec(RecType.USER));
        assertFalse(c.isEmptyRec(RecType.USER));
        assertEquals("BJONES", ((Rec) c.getFirstRec(RecType.USER)).getMnem());
        assertEquals("EEDWARDS", ((Rec) c.getFirstRec(RecType.USER).slNext()).getMnem());
        assertEquals("GWILSON", c.findRecMnem(RecType.USER, "GWILSON").getMnem());
        assertEquals("JTHOMAS", c.findRecId(RecType.USER, 4).getMnem());
    }
}

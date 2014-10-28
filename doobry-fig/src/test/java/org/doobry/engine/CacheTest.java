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
        assertTrue(c.isEmpty(RecType.ASSET));
        assertTrue(c.isEmpty(RecType.CONTR));
        assertTrue(c.isEmpty(RecType.PARTY));
        assertNull(c.getFirst(RecType.ASSET));
        assertNull(c.getFirst(RecType.CONTR));
        assertNull(c.getFirst(RecType.PARTY));
    }

    @Test
    public final void testAsset() {
        final Cache c = new Cache(257);
        c.insertList(model.readRec(RecType.ASSET));
        assertFalse(c.isEmpty(RecType.ASSET));
        assertEquals("AUD", c.getFirst(RecType.ASSET).getMnem());
        assertEquals("CAD", ((Rec) c.getFirst(RecType.ASSET).slNext()).getMnem());
        assertEquals("CHF", c.findMnem(RecType.ASSET, "CHF").getMnem());
        assertEquals("CZK", c.findId(RecType.ASSET, 4).getMnem());
    }

    @Test
    public final void testContr() {
        final Cache c = new Cache(257);
        c.insertList(model.readRec(RecType.CONTR));
        assertFalse(c.isEmpty(RecType.CONTR));
        assertEquals("AUDUSD", c.getFirst(RecType.CONTR).getMnem());
        assertEquals("EURCHF", ((Rec) c.getFirst(RecType.CONTR).slNext()).getMnem());
        assertEquals("EURCZK", c.findMnem(RecType.CONTR, "EURCZK").getMnem());
        assertEquals("EURDKK", c.findId(RecType.CONTR, 4).getMnem());
    }

    @Test
    public final void testParty() {
        final Cache c = new Cache(257);
        c.insertList(model.readRec(RecType.PARTY));
        assertFalse(c.isEmpty(RecType.PARTY));
        assertEquals("BJONES", c.getFirst(RecType.PARTY).getMnem());
        assertEquals("DBRA", ((Rec) c.getFirst(RecType.PARTY).slNext()).getMnem());
        assertEquals("DBRB", c.findMnem(RecType.PARTY, "DBRB").getMnem());
        assertEquals("EEDWARDS", c.findId(RecType.PARTY, 4).getMnem());
    }
}

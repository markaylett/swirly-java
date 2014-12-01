/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.domain.RecType;
import com.swirlycloud.domain.User;
import com.swirlycloud.function.UnaryCallback;
import com.swirlycloud.mock.MockModel;

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
        model.selectAsset(new UnaryCallback<Asset>() {
            @Override
            public final void call(Asset arg) {
                c.insertRec(arg);
            }
        });
        assertFalse(c.isEmptyRec(RecType.ASSET));
        assertEquals("AUD", ((Rec) c.getFirstRec(RecType.ASSET)).getMnem());
        assertEquals("CAD", ((Rec) c.getFirstRec(RecType.ASSET).slNext()).getMnem());
        assertEquals("CHF", c.findRec(RecType.ASSET, "CHF").getMnem());
        assertEquals("CZK", c.findRec(RecType.ASSET, 4).getMnem());
    }

    @Test
    public final void testContr() {
        final Cache c = new Cache(257);
        model.selectContr(new UnaryCallback<Contr>() {
            @Override
            public final void call(Contr arg) {
                c.insertRec(arg);
            }
        });
        assertFalse(c.isEmptyRec(RecType.CONTR));
        assertEquals("AUDUSD", ((Rec) c.getFirstRec(RecType.CONTR)).getMnem());
        assertEquals("EURCHF", ((Rec) c.getFirstRec(RecType.CONTR).slNext()).getMnem());
        assertEquals("EURCZK", c.findRec(RecType.CONTR, "EURCZK").getMnem());
        assertEquals("EURDKK", c.findRec(RecType.CONTR, 4).getMnem());
    }

    @Test
    public final void testUser() {
        final Cache c = new Cache(257);
        model.selectUser(new UnaryCallback<User>() {
            @Override
            public final void call(User arg) {
                c.insertRec(arg);
            }
        });
        assertFalse(c.isEmptyRec(RecType.USER));
        assertEquals("EMIAYL", ((Rec) c.getFirstRec(RecType.USER)).getMnem());
        assertEquals("GOSAYL", ((Rec) c.getFirstRec(RecType.USER).slNext()).getMnem());
        assertEquals("MARAYL", c.findRec(RecType.USER, "MARAYL").getMnem());
        assertEquals("TOBAYL", c.findRec(RecType.USER, 3).getMnem());
    }
}

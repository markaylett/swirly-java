/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.twirly.app.Cache;
import com.swirlycloud.twirly.app.Model;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.mock.MockModel;

public final class CacheTest {
    private final Model model = new MockModel();

    @Test
    public final void testEmpty() {
        final Cache c = new Cache(257);
        assertTrue(c.isEmptyRec(RecType.ASSET));
        assertTrue(c.isEmptyRec(RecType.CONTR));
        assertTrue(c.isEmptyRec(RecType.TRADER));
        assertNull(c.getFirstRec(RecType.ASSET));
        assertNull(c.getFirstRec(RecType.CONTR));
        assertNull(c.getFirstRec(RecType.TRADER));
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
    public final void testTrader() {
        final Cache c = new Cache(257);
        model.selectTrader(new UnaryCallback<Trader>() {
            @Override
            public final void call(Trader arg) {
                c.insertRec(arg);
            }
        });
        assertFalse(c.isEmptyRec(RecType.TRADER));
        assertEquals("MARAYL", ((Rec) c.getFirstRec(RecType.TRADER)).getMnem());
        assertEquals("GOSAYL", ((Rec) c.getFirstRec(RecType.TRADER).slNext()).getMnem());
        assertEquals("TOBAYL", c.findRec(RecType.TRADER, "TOBAYL").getMnem());
        assertEquals("EMIAYL", c.findRec(RecType.TRADER, 4).getMnem());
    }
}

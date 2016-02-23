/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.swirly.entity.EntitySet;

public final class EntitySetTest {
    @Test
    public final void testZero() {
        final EntitySet set = EntitySet.parse("");
        assertFalse(set.hasMany());
        assertFalse(set.isAssetSet());
        assertFalse(set.isContrSet());
        assertFalse(set.isMarketSet());
        assertFalse(set.isTraderSet());

        // End.
        assertFalse(set.hasNext());
    }

    @Test
    public final void testOne() {
        final EntitySet set = EntitySet.parse("asset");
        assertFalse(set.hasMany());
        assertTrue(set.isAssetSet());
        assertFalse(set.isContrSet());
        assertFalse(set.isMarketSet());
        assertFalse(set.isTraderSet());

        // First.
        assertTrue(set.hasNext());
        assertEquals(EntitySet.ASSET, set.next());

        // End.
        assertFalse(set.hasNext());
    }

    @Test
    public final void testTwo() {
        final EntitySet set = EntitySet.parse("asset,contr");
        assertTrue(set.hasMany());
        assertTrue(set.isAssetSet());
        assertTrue(set.isContrSet());
        assertFalse(set.isMarketSet());
        assertFalse(set.isTraderSet());

        // First.
        assertTrue(set.hasNext());
        assertEquals(EntitySet.ASSET, set.next());

        // Second.
        assertTrue(set.hasNext());
        assertEquals(EntitySet.CONTR, set.next());

        // End.
        assertFalse(set.hasNext());
    }

    @Test
    public final void testThree() {
        final EntitySet set = EntitySet.parse("market,contr,asset");
        assertTrue(set.hasMany());
        assertTrue(set.isAssetSet());
        assertTrue(set.isContrSet());
        assertTrue(set.isMarketSet());
        assertFalse(set.isTraderSet());

        // First.
        assertTrue(set.hasNext());
        assertEquals(EntitySet.ASSET, set.next());

        // Second.
        assertTrue(set.hasNext());
        assertEquals(EntitySet.CONTR, set.next());

        // Third.
        assertTrue(set.hasNext());
        assertEquals(EntitySet.MARKET, set.next());

        // End.
        assertFalse(set.hasNext());
    }

    @Test
    public final void testTrailing() {
        final EntitySet set = EntitySet.parse("trader,");
        assertFalse(set.hasMany());
        assertFalse(set.isAssetSet());
        assertFalse(set.isContrSet());
        assertFalse(set.isMarketSet());
        assertTrue(set.isTraderSet());

        // First.
        assertTrue(set.hasNext());
        assertEquals(EntitySet.TRADER, set.next());

        // End.
        assertFalse(set.hasNext());
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class MarketIdTest {
    @Test
    public final void testZero() {
        // End.
        final MarketId mid = MarketId.parse("EURUSD", "");
        assertNull(mid);
    }

    @Test
    public final void testOne() {
        // First.
        MarketId mid = MarketId.parse("EURUSD", "101");
        assertNotNull(mid);
        assertEquals(101, mid.getId());

        // End.
        mid = (MarketId) mid.jslNext();
        assertNull(mid);
    }

    @Test
    public final void testTwo() {
        // Second.
        MarketId mid = MarketId.parse("EURUSD", "101,202");
        assertNotNull(mid);
        assertEquals(202, mid.getId());

        // First.
        mid = (MarketId) mid.jslNext();
        assertNotNull(mid);
        assertEquals(101, mid.getId());

        // End.
        mid = (MarketId) mid.jslNext();
        assertNull(mid);
    }

    @Test
    public final void testThree() {
        // Third.
        MarketId mid = MarketId.parse("EURUSD", "101,202,303");
        assertNotNull(mid);
        assertEquals(303, mid.getId());

        // Second.
        mid = (MarketId) mid.jslNext();
        assertNotNull(mid);
        assertEquals(202, mid.getId());

        // First.
        mid = (MarketId) mid.jslNext();
        assertNotNull(mid);
        assertEquals(101, mid.getId());

        // End.
        mid = (MarketId) mid.jslNext();
        assertNull(mid);
    }

    @Test
    public final void testTrailing() {
        // First.
        MarketId mid = MarketId.parse("EURUSD", "101,");
        assertNotNull(mid);
        assertEquals(101, mid.getId());

        // End.
        mid = (MarketId) mid.jslNext();
        assertNull(mid);
    }
}

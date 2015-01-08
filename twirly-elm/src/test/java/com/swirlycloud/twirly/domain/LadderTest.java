/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LadderTest {

    private static final double DELTA = 0.000001;

    @Test
    public final void testAccessors() {
        final Ladder ladder = new Ladder();
        ladder.setBidCount(0, 2);
        ladder.setBidLots(1, 10);
        ladder.setBidTicks(2, 12345.5);
        ladder.setOfferTicks(0, 12347.5);
        ladder.setOfferLots(1, 15);
        ladder.setOfferCount(2, 3);
        assertEquals(2, ladder.getBidCount(0), DELTA);
        assertEquals(10, ladder.getBidLots(1), DELTA);
        assertEquals(12345.5, ladder.getBidTicks(2), DELTA);
        assertEquals(12347.5, ladder.getOfferTicks(0), DELTA);
        assertEquals(15, ladder.getOfferLots(1), DELTA);
        assertEquals(3, ladder.getOfferCount(2), DELTA);
    }

    @Test
    public final void testRounding() {
        final Ladder ladder = new Ladder();
        ladder.setBidTicks(0, 12345.5);
        ladder.setOfferTicks(0, 12347.5);
        // Bid should round down.
        assertEquals(12345, ladder.roundBidTicks(0), DELTA);
        // Offer should round up.
        assertEquals(12348, ladder.roundOfferTicks(0), DELTA);
    }
}

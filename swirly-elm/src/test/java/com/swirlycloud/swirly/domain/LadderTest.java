/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.domain;

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

    @Test
    public final void testVwap() {
        final Ladder ladder = new Ladder();

        ladder.setOfferLevel(2, 12348, 30, 3);
        ladder.setOfferLevel(1, 12347, 20, 2);
        ladder.setOfferLevel(0, 12346, 10, 1);

        ladder.setBidLevel(0, 12344, 10, 1);
        ladder.setBidLevel(1, 12343, 20, 2);
        ladder.setBidLevel(2, 12342, 30, 3);

        ladder.setVwap(true);

        // 12344.00 10.00000 1.000000 12346.00 10.00000 1.000000

        assertEquals(12344, ladder.getBidTicks(0), DELTA);
        assertEquals(10, ladder.getBidLots(0), DELTA);
        assertEquals(1, ladder.getBidCount(0), DELTA);
        assertEquals(12346, ladder.getOfferTicks(0), DELTA);
        assertEquals(10, ladder.getOfferLots(0), DELTA);
        assertEquals(1, ladder.getOfferCount(0), DELTA);

        // 12343.33 30.00000 3.000000 12346.67 30.00000 3.000000

        assertEquals(12343.33, ladder.getBidTicks(1), 0.01);
        assertEquals(30, ladder.getBidLots(1), DELTA);
        assertEquals(3, ladder.getBidCount(1), DELTA);
        assertEquals(12346.67, ladder.getOfferTicks(1), 0.01);
        assertEquals(30, ladder.getOfferLots(1), DELTA);
        assertEquals(3, ladder.getOfferCount(1), DELTA);

        // 12342.67 60.00000 6.000000 12347.33 60.00000 6.000000

        assertEquals(12342.67, ladder.getBidTicks(2), 0.01);
        assertEquals(60, ladder.getBidLots(2), DELTA);
        assertEquals(6, ladder.getBidCount(2), DELTA);
        assertEquals(12347.33, ladder.getOfferTicks(2), 0.01);
        assertEquals(60, ladder.getOfferLots(2), DELTA);
        assertEquals(6, ladder.getOfferCount(2), DELTA);

        ladder.setVwap(false);

        // 12344.00 10.00000 1.000000 12346.00 10.00000 1.000000

        assertEquals(12344, ladder.getBidTicks(0), DELTA);
        assertEquals(10, ladder.getBidLots(0), DELTA);
        assertEquals(1, ladder.getBidCount(0), DELTA);
        assertEquals(12346, ladder.getOfferTicks(0), DELTA);
        assertEquals(10, ladder.getOfferLots(0), DELTA);
        assertEquals(1, ladder.getOfferCount(0), DELTA);

        // 12343.00 20.00000 2.000000 12347.00 20.00000 2.000000

        assertEquals(12343.00, ladder.getBidTicks(1), 0.01);
        assertEquals(20, ladder.getBidLots(1), DELTA);
        assertEquals(2, ladder.getBidCount(1), DELTA);
        assertEquals(12347.00, ladder.getOfferTicks(1), 0.01);
        assertEquals(20, ladder.getOfferLots(1), DELTA);
        assertEquals(2, ladder.getOfferCount(1), DELTA);

        // 12342.00 30.00000 3.000000 12348.00 30.00000 3.000000

        assertEquals(12342.00, ladder.getBidTicks(2), 0.01);
        assertEquals(30, ladder.getBidLots(2), DELTA);
        assertEquals(3, ladder.getBidCount(2), DELTA);
        assertEquals(12348, ladder.getOfferTicks(2), 0.01);
        assertEquals(30, ladder.getOfferLots(2), DELTA);
        assertEquals(3, ladder.getOfferCount(2), DELTA);
    }
}

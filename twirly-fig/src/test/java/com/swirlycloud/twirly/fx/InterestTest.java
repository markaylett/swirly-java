/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class InterestTest {
    private static final double DELTA = 0.0000001;

    @Test
    public final void testSimpleInterest() {
        final Interest i = Interest.SIMPLE_INTEREST;
        // Pv is the inverse of fv.
        assertEquals(100, 100 * i.fv(2, 0.05) * i.pv(2, 0.05), DELTA);
        assertEquals(0.05, i.ir(2, i.fv(2, 0.05)), DELTA);
    }

    @Test
    public final void testAnnualComp() {
        final Interest i = Interest.ANNUAL_COMP;
        // Pv is the inverse of fv.
        assertEquals(100, 100 * i.fv(2, 0.05) * i.pv(2, 0.05), DELTA);
        assertEquals(0.05, i.ir(2, i.fv(2, 0.05)), DELTA);
    }

    @Test
    public final void testContComp() {
        final Interest i = Interest.CONT_COMP;
        // Pv is the inverse of fv.
        assertEquals(100, 100 * i.fv(2, 0.05) * i.pv(2, 0.05), DELTA);
        assertEquals(0.05, i.ir(2, i.fv(2, 0.05)), DELTA);
    }

    @Test
    public final void testPeriodComp() {
        final Interest i = Interest.newPeriodComp(12);
        // Pv is the inverse of fv.
        assertEquals(100, 100 * i.fv(2, 0.05) * i.pv(2, 0.05), DELTA);
        assertEquals(0.05, i.ir(2, i.fv(2, 0.05)), DELTA);
    }
}

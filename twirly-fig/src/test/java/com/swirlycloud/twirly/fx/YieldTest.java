/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class YieldTest {
    private static final double DELTA = 0.000000000001;

    private static void pv(Yield y) {
        // Pv is the inverse of fv.
        assertEquals(100, 100 * y.fv(0.05, 2) * y.pv(0.05, 2), DELTA);
    }

    private static void ir(Yield y) {
        assertEquals(0.05, y.ir(y.fv(0.05, 2), 2), DELTA);
    }

    private static void fr(Yield y) {
        assertEquals(y.fv(0.07, 5), y.fv(0.05, 3) * y.fv(y.fr(0.05, 3, 0.07, 5), 2), DELTA);
    }

    @Test
    public final void testSimpleInterestFv() {
        final Yield y = Yield.SIMPLE_INTEREST;
        assertEquals(110, 100 * y.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testSimpleInterestPv() {
        pv(Yield.SIMPLE_INTEREST);
    }

    @Test
    public final void testSimpleInterestIr() {
        ir(Yield.SIMPLE_INTEREST);
    }

    @Test
    public final void testSimpleInterestFr() {
        fr(Yield.SIMPLE_INTEREST);
    }

    @Test
    public final void testAnnualCompFv() {
        final Yield y = Yield.ANNUAL_COMP;
        assertEquals(110.25, 100 * y.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testAnnualCompPv() {
        pv(Yield.ANNUAL_COMP);
    }

    @Test
    public final void testAnnualCompIr() {
        ir(Yield.ANNUAL_COMP);
    }

    @Test
    public final void testAnnualCompFr() {
        fr(Yield.ANNUAL_COMP);
    }

    @Test
    public final void testContCompFv() {
        final Yield y = Yield.CONT_COMP;
        assertEquals(110.51709180756477, 100 * y.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testContCompPv() {
        pv(Yield.CONT_COMP);
    }

    @Test
    public final void testContCompIr() {
        ir(Yield.CONT_COMP);
    }

    @Test
    public final void testContCompFr() {
        fr(Yield.CONT_COMP);
    }

    @Test
    public final void testPeriodCompFv() {
        final Yield y = Yield.newPeriodComp(12);
        assertEquals(110.49413355583269, 100 * y.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testPeriodCompPv() {
        pv(Yield.SEMI_ANNUAL);
        pv(Yield.newPeriodComp(4));
        pv(Yield.newPeriodComp(12));
    }

    @Test
    public final void testPeriodCompIr() {
        ir(Yield.SEMI_ANNUAL);
        ir(Yield.newPeriodComp(4));
        ir(Yield.newPeriodComp(12));
    }

    @Test
    public final void testPeriodCompFr() {
        fr(Yield.SEMI_ANNUAL);
        fr(Yield.newPeriodComp(4));
        fr(Yield.newPeriodComp(12));
    }
}

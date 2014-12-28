/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class IrCalcTest {
    private static final double DELTA = 0.000000000001;

    private static void pv(IrCalc irc) {
        // Pv is the inverse of fv.
        assertEquals(100, 100 * irc.fv(0.05, 2) * irc.pv(0.05, 2), DELTA);
    }

    private static void ir(IrCalc irc) {
        assertEquals(0.05, irc.ir(irc.fv(0.05, 2), 2), DELTA);
    }

    private static void fr(IrCalc irc) {
        assertEquals(irc.fv(0.07, 5), irc.fv(0.05, 3) * irc.fv(irc.fr(0.05, 3, 0.07, 5), 2), DELTA);
    }

    @Test
    public final void testSimpleInterestFv() {
        final IrCalc irc = IrCalc.SIMPLE_INTEREST;
        assertEquals(110, 100 * irc.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testSimpleInterestPv() {
        pv(IrCalc.SIMPLE_INTEREST);
    }

    @Test
    public final void testSimpleInterestIr() {
        ir(IrCalc.SIMPLE_INTEREST);
    }

    @Test
    public final void testSimpleInterestFr() {
        fr(IrCalc.SIMPLE_INTEREST);
    }

    @Test
    public final void testAnnualCompFv() {
        final IrCalc irc = IrCalc.ANNUAL_COMP;
        assertEquals(110.25, 100 * irc.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testAnnualCompPv() {
        pv(IrCalc.ANNUAL_COMP);
    }

    @Test
    public final void testAnnualCompIr() {
        ir(IrCalc.ANNUAL_COMP);
    }

    @Test
    public final void testAnnualCompFr() {
        fr(IrCalc.ANNUAL_COMP);
    }

    @Test
    public final void testContCompFv() {
        final IrCalc irc = IrCalc.CONT_COMP;
        assertEquals(110.51709180756477, 100 * irc.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testContCompPv() {
        pv(IrCalc.CONT_COMP);
    }

    @Test
    public final void testContCompIr() {
        ir(IrCalc.CONT_COMP);
    }

    @Test
    public final void testContCompFr() {
        fr(IrCalc.CONT_COMP);
    }

    @Test
    public final void testPeriodCompFv() {
        final IrCalc irc = IrCalc.newPeriodComp(12);
        assertEquals(110.49413355583269, 100 * irc.fv(0.05, 2), DELTA);
    }

    @Test
    public final void testPeriodCompPv() {
        pv(IrCalc.SEMI_ANNUAL);
        pv(IrCalc.newPeriodComp(4));
        pv(IrCalc.newPeriodComp(12));
    }

    @Test
    public final void testPeriodCompIr() {
        ir(IrCalc.SEMI_ANNUAL);
        ir(IrCalc.newPeriodComp(4));
        ir(IrCalc.newPeriodComp(12));
    }

    @Test
    public final void testPeriodCompFr() {
        fr(IrCalc.SEMI_ANNUAL);
        fr(IrCalc.newPeriodComp(4));
        fr(IrCalc.newPeriodComp(12));
    }
}

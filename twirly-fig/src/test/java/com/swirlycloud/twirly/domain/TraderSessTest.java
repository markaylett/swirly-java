/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import com.swirlycloud.twirly.mock.MockTrader;

public final class TraderSessTest {

    private static final int TODAY = ymdToJd(2014, 2, 12);
    private static final int SETTL_DAY = TODAY + 2;

    private Factory factory;
    private MockTrader mockTrader;

    @Before
    public final void setUp() {
        factory = new ServFactory();
        mockTrader = new MockTrader(factory);
    }

    @Test
    public final void testAdd() {
        final TraderSess sess = (TraderSess) mockTrader.newTrader("MARAYL");
        final Posn posn1 = factory.newPosn(sess.getMnem(), "EURUSD", SETTL_DAY);
        posn1.addBuy(12344 * 10, 10);
        posn1.addSell(12346 * 15, 15);
        assertSame(posn1, sess.addPosn(posn1));
        final Posn posn2 = factory.newPosn(sess.getMnem(), "EURUSD", SETTL_DAY);
        posn2.addBuy(12343 * 15, 15);
        posn2.addSell(12347 * 10, 10);
        assertSame(posn1, sess.addPosn(posn2));
        assertEquals(12344 * 10 + 12343 * 15, posn1.getBuyCost());
        assertEquals(25, posn1.getBuyLots());
        assertEquals(12346 * 15 + 12347 * 10, posn1.getSellCost());
        assertEquals(25, posn1.getSellLots());
    }
}

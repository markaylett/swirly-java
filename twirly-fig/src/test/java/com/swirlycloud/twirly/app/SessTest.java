/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import com.swirlycloud.twirly.domain.BasicFactory;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.intrusive.RefHashTable;
import com.swirlycloud.twirly.mock.MockTrader;

public final class SessTest {

    private static final @NonNull Factory FACTORY = new BasicFactory();
    private static final MockTrader MOCK_TRADER = new MockTrader(FACTORY);

    private static final int CAPACITY = 1 << 5; // 64
    private static final int TODAY = ymdToJd(2014, 2, 12);
    private static final int SETTL_DAY = TODAY + 2;

    @Test
    public final void testAdd() {
        final Trader trader = MOCK_TRADER.newTrader("MARAYL");
        final RefHashTable refIdx = new RefHashTable(CAPACITY);
        final Sess sess = new Sess(trader, refIdx, FACTORY);
        final Posn posn1 = FACTORY.newPosn(trader.getMnem(), "EURUSD", SETTL_DAY);
        posn1.addBuy(12344 * 10, 10);
        posn1.addSell(12346 * 15, 15);
        assertSame(posn1, sess.addPosn(posn1));
        final Posn posn2 = FACTORY.newPosn(trader.getMnem(), "EURUSD", SETTL_DAY);
        posn2.addBuy(12343 * 15, 15);
        posn2.addSell(12347 * 10, 10);
        assertSame(posn1, sess.addPosn(posn2));
        assertEquals(12344 * 10 + 12343 * 15, posn1.getBuyCost());
        assertEquals(25, posn1.getBuyLots());
        assertEquals(12346 * 15 + 12347 * 10, posn1.getSellCost());
        assertEquals(25, posn1.getSellLots());
    }
}

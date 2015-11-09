/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class QuoteTest {
    private static final Factory FACTORY = new BasicFactory();

    private static final int TODAY = ymdToJd(2014, 2, 12);
    private static final int SETTL_DAY = TODAY + 2;
    private static final long NOW = jdToMillis(TODAY);
    private static final long EXPIRY = NOW + 60 * 1000;

    private static Quote newQuote() {
        return FACTORY.newQuote("MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, 1, "test", Side.BUY,
                10, 12345, NOW, EXPIRY);
    }

    @Test
    public final void testContruct() {
        final Quote quote = newQuote();

        assertEquals("MARAYL", quote.getTrader());
        assertEquals("EURUSD.MAR14", quote.getMarket());
        assertEquals("EURUSD", quote.getContr());
        assertEquals(SETTL_DAY, quote.getSettlDay());
        assertEquals(1, quote.getId());
        assertEquals("test", quote.getRef());
        assertEquals(Side.BUY, quote.getSide());
        assertEquals(10, quote.getLots());
        assertEquals(12345, quote.getTicks());
        assertEquals(NOW, quote.getCreated());
        assertEquals(EXPIRY, quote.getExpiry());
    }

    @Test
    public final void testToString() {
        final Quote quote = newQuote();
        assertEquals(
                "{\"trader\":\"MARAYL\",\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"id\":1,\"ref\":\"test\",\"side\":\"BUY\",\"lots\":10,\"ticks\":12345,\"created\":1394625600000,\"expiry\":1394625660000}",
                quote.toString());
    }
}

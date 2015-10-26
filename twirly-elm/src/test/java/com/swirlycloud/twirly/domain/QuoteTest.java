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
        return FACTORY.newQuote(1, "MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, "test", 12344, 5,
                12346, 10, NOW, EXPIRY);
    }

    @Test
    public final void testContruct() {
        final Quote quote = newQuote();

        assertEquals(1, quote.getId());
        assertEquals("MARAYL", quote.getTrader());
        assertEquals("EURUSD.MAR14", quote.getMarket());
        assertEquals("EURUSD", quote.getContr());
        assertEquals(SETTL_DAY, quote.getSettlDay());
        assertEquals("test", quote.getRef());
        assertEquals(12344, quote.getBidTicks());
        assertEquals(5, quote.getBidLots());
        assertEquals(12346, quote.getOfferTicks());
        assertEquals(10, quote.getOfferLots());
        assertEquals(NOW, quote.getCreated());
        assertEquals(EXPIRY, quote.getExpiry());
    }

    @Test
    public final void testToString() {
        final Quote quote = newQuote();
        assertEquals(
                "{\"id\":1,\"trader\":\"MARAYL\",\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"ref\":\"test\"\",\"bidTicks\":12344,\"bidLots\":5\",\"offerTicks\":12346,\"offerLots\":10,\"created\":1394625600000,\"expiry\":1394625660000}",
                quote.toString());
    }
}

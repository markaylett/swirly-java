/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.entity.Quote;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;

public final class QuoteRestTest extends RestTest {

    // Get Quote.

    @Test
    public final void testGetAll() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", 0, Side.SELL, 10, 12345);
        postQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10);
        final Map<Long, Quote> out = unrest.getQuote("MARAYL", PARAMS_NONE, NOW);
        assertEquals(1, out.size());
        assertQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10, 12345, out.get(Long.valueOf(1)));
        assertTrue(unrest.getQuote("GOSAYL", PARAMS_NONE, NOW).isEmpty());
    }

    @Test
    public final void testGetByMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", 0, Side.SELL, 10, 12345);
        postQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10);
        final Map<Long, Quote> out = unrest.getQuote("MARAYL", "EURUSD.MAR14", PARAMS_NONE, NOW);
        assertEquals(1, out.size());
        assertQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10, 12345, out.get(Long.valueOf(1)));
        assertTrue(unrest.getQuote("MARAYL", "USDJPY.MAR14", PARAMS_NONE, NOW).isEmpty());
    }

    @Test
    public final void testGetByMarketId() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", 0, Side.SELL, 10, 12345);
        postQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10);
        final Quote out = unrest.getQuote("MARAYL", "EURUSD.MAR14", 1, PARAMS_NONE, NOW);
        assertQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10, 12345, out);
        try {
            unrest.getQuote("MARAYL", "EURUSD.MAR14", 2, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    // Create Quote.

    @Test
    public final void testCreate() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", 0, Side.SELL, 10, 12345);
        final Quote quote = postQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10);
        assertQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10, 12345, quote);
    }
}

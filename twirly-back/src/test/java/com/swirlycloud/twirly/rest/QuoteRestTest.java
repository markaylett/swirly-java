/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;

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
        final Quote quote = postQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10);
        assertQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10, 12345, quote);
        final Map<Long, Quote> out = unrest.getQuote("MARAYL", PARAMS_NONE, NOW);
        assertEquals(1, out.size());
        assertQuote("MARAYL", "EURUSD.MAR14", Side.BUY, 10, 12345, out.get(Long.valueOf(1)));
    }
}

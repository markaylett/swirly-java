/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;

public final class TradeRestTest extends RestTest {

    @Test
    public final void testGetTrade() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 12345, 10);
        final Map<Long, Exec> out = unrest.getTrade("MARAYL", PARAMS_NONE, NOW);
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.SELL, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.MAKER, "MARAYL", out.get(Long.valueOf(3)));
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.BUY, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.TAKER, "MARAYL", out.get(Long.valueOf(4)));
    }

    @Test
    public final void testGetTradeMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 12345, 10);
        final Map<Long, Exec> out = unrest.getTrade("MARAYL", "EURUSD.MAR14", PARAMS_NONE, NOW);
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.SELL, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.MAKER, "MARAYL", out.get(Long.valueOf(3)));
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.BUY, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.TAKER, "MARAYL", out.get(Long.valueOf(4)));
        assertTrue(unrest.getTrade("MARAYL", "USDJPY.MAR14", PARAMS_NONE, NOW).isEmpty());
    }

    @Test
    public final void testGetTradeMarketId() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 12345, 10);
        final Map<Long, Exec> out = unrest.getTrade("MARAYL", "EURUSD.MAR14", PARAMS_NONE, NOW);
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.SELL, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.MAKER, "MARAYL", out.get(Long.valueOf(3)));
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.BUY, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.TAKER, "MARAYL", out.get(Long.valueOf(4)));
        try {
            unrest.getOrder("MARAYL", "EURUSD.MAR14", 3, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
        try {
            unrest.getOrder("MARAYL", "EURUSD.MAR14", 4, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testDeleteTrade() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 12345, 10);
        final Exec trade = unrest.getTrade("MARAYL", "EURUSD.MAR14", 3, PARAMS_NONE, NOW);
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.SELL, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.MAKER, "MARAYL", trade);
        deleteTrade("MARAYL", "EURUSD.MAR14", 3);
        try {
            unrest.getTrade("MARAYL", "EURUSD.MAR14", 3, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Ignore("not implemented")
    @Test
    public final void testDeleteTradeList() {
        // FIXME
    }

    @Test
    public final void testPostTrade() throws NotFoundException, ServiceUnavailableException,
            IOException {
        final Exec exec = unrest.postTrade("MARAYL", "EURUSD.MAR14", "", Side.BUY, 12345, 10,
                Role.MAKER, "MARAYL", PARAMS_NONE, NOW);
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.BUY, 12345, 10, 0, 10, 123450,
                12345, 10, "EURUSD", SETTL_DAY, Role.MAKER, "MARAYL", exec);
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.swirlycloud.swirly.domain.Role;
import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.domain.State;
import com.swirlycloud.swirly.entity.Exec;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;

public final class MatchRestTest extends RestTest {

    // Match Trade.

    @Test
    public final void testWithSelf() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MS);
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.BUY, 10, 12345, TODAY_MS);
        final Map<Long, Exec> out = unrest.getTrade(MARAYL, PARAMS_NONE, TODAY_MS);
        assertEquals(2, out.size());
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.SELL, 10, 12345, 0, 10, 123450, 10,
                12345, EURUSD, SETTL_DAY, Role.MAKER, MARAYL, out.get(Long.valueOf(3)));
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.BUY, 10, 12345, 0, 10, 123450, 10, 12345,
                EURUSD, SETTL_DAY, Role.TAKER, MARAYL, out.get(Long.valueOf(4)));
    }

    @Test
    public final void testWithCpty() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MS);
        postOrder(GOSAYL, EURUSD_MAR14, 0, Side.BUY, 10, 12345, TODAY_MS);
        Map<Long, Exec> out = unrest.getTrade(MARAYL, PARAMS_NONE, TODAY_MS);
        assertEquals(1, out.size());
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.SELL, 10, 12345, 0, 10, 123450, 10,
                12345, EURUSD, SETTL_DAY, Role.MAKER, GOSAYL, out.get(Long.valueOf(3)));
        out = unrest.getTrade(GOSAYL, PARAMS_NONE, TODAY_MS);
        assertEquals(1, out.size());
        assertExec(GOSAYL, EURUSD_MAR14, State.TRADE, Side.BUY, 10, 12345, 0, 10, 123450, 10, 12345,
                EURUSD, SETTL_DAY, Role.TAKER, MARAYL, out.get(Long.valueOf(4)));
    }

    @Test
    public final void testPartial() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MS);
        postOrder(GOSAYL, EURUSD_MAR14, 0, Side.BUY, 3, 12345, TODAY_MS);
        Map<Long, Exec> out = unrest.getTrade(MARAYL, PARAMS_NONE, TODAY_MS);
        assertEquals(1, out.size());
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.SELL, 10, 12345, 7, 3, 37035, 3, 12345,
                EURUSD, SETTL_DAY, Role.MAKER, GOSAYL, out.get(Long.valueOf(3)));
        out = unrest.getTrade(GOSAYL, PARAMS_NONE, TODAY_MS);
        assertEquals(1, out.size());
        assertExec(GOSAYL, EURUSD_MAR14, State.TRADE, Side.BUY, 3, 12345, 0, 3, 37035, 3, 12345,
                EURUSD, SETTL_DAY, Role.TAKER, MARAYL, out.get(Long.valueOf(4)));
    }

    @Test
    public final void testSweep() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MS);
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12346, TODAY_MS);
        postOrder(GOSAYL, EURUSD_MAR14, 0, Side.BUY, 20, 12346, TODAY_MS);
        Map<Long, Exec> out = unrest.getTrade(MARAYL, PARAMS_NONE, TODAY_MS);
        assertEquals(2, out.size());
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.SELL, 10, 12345, 0, 10, 123450, 10,
                12345, EURUSD, SETTL_DAY, Role.MAKER, GOSAYL, out.get(Long.valueOf(4)));
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.SELL, 10, 12346, 0, 10, 123460, 10,
                12346, EURUSD, SETTL_DAY, Role.MAKER, GOSAYL, out.get(Long.valueOf(6)));
        out = unrest.getTrade(GOSAYL, PARAMS_NONE, TODAY_MS);
        assertEquals(2, out.size());
        assertExec(GOSAYL, EURUSD_MAR14, State.TRADE, Side.BUY, 20, 12346, 10, 10, 123450, 10,
                12345, EURUSD, SETTL_DAY, Role.TAKER, MARAYL, out.get(Long.valueOf(5)));
        assertExec(GOSAYL, EURUSD_MAR14, State.TRADE, Side.BUY, 20, 12346, 0, 20, 246910, 10, 12346,
                EURUSD, SETTL_DAY, Role.TAKER, MARAYL, out.get(Long.valueOf(7)));
    }
}

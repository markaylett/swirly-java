/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.rest.BackUnrest.PosnKey;

public final class PosnRestTest extends RestTest {

    @Test
    public final void testGetPosn() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 12345, 10);
        postOrder("MARAYL", "USDJPY.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "USDJPY.MAR14", Side.BUY, 12345, 10);
        final Map<PosnKey, Posn> out = unrest.getPosn("MARAYL", PARAMS_NONE, NOW);
        assertEquals(out.size(), 2);
        assertPosn("MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, 123450, 10, 123450, 10,
                out.get(new PosnKey("EURUSD", SETTL_DAY)));
        assertPosn("MARAYL", "USDJPY.MAR14", "USDJPY", SETTL_DAY, 123450, 10, 123450, 10,
                out.get(new PosnKey("USDJPY", SETTL_DAY)));
    }

    @Test
    public final void testGetPosnContr() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 12345, 10);
        postOrder("MARAYL", "USDJPY.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "USDJPY.MAR14", Side.BUY, 12345, 10);
        final Map<PosnKey, Posn> out = unrest.getPosn("MARAYL", "EURUSD", PARAMS_NONE, NOW);
        assertEquals(out.size(), 1);
        assertPosn("MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, 123450, 10, 123450, 10,
                out.get(new PosnKey("EURUSD", SETTL_DAY)));
    }

    @Test
    public final void testGetPosnContrSettlDay() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 12345, 10);
        final Posn posn = unrest.getPosn("MARAYL", "EURUSD", SETTL_DAY, PARAMS_NONE, NOW);
        assertPosn("MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, 123450, 10, 123450, 10, posn);
        try {
            unrest.getPosn("MARAYL", "USDJPY", SETTL_DAY, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.entity.Posn;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.rest.BackUnrest.PosnKey;

public final class PosnRestTest extends RestTest {

    // Get Posn.

    @Test
    public final void testGetAll() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.BUY, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, USDJPY_MAR14, 0, Side.SELL, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, USDJPY_MAR14, 0, Side.BUY, 10, 12345, TODAY_MILLIS);
        final Map<PosnKey, Posn> out = unrest.getPosn(MARAYL, PARAMS_NONE, TODAY_MILLIS);
        assertEquals(out.size(), 2);
        assertPosn(MARAYL, EURUSD_MAR14, EURUSD, SETTL_DAY, 10, 123450, 10, 123450,
                out.get(new PosnKey(EURUSD, SETTL_DAY)));
        assertPosn(MARAYL, USDJPY_MAR14, USDJPY, SETTL_DAY, 10, 123450, 10, 123450,
                out.get(new PosnKey(USDJPY, SETTL_DAY)));
    }

    @Test
    public final void testGetByContr() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.BUY, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, USDJPY_MAR14, 0, Side.SELL, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, USDJPY_MAR14, 0, Side.BUY, 10, 12345, TODAY_MILLIS);
        final Map<PosnKey, Posn> out = unrest.getPosn(MARAYL, EURUSD, PARAMS_NONE, TODAY_MILLIS);
        assertEquals(out.size(), 1);
        assertPosn(MARAYL, EURUSD_MAR14, EURUSD, SETTL_DAY, 10, 123450, 10, 123450,
                out.get(new PosnKey(EURUSD, SETTL_DAY)));
    }

    @Test
    public final void testGetByContrSettlDay() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.BUY, 10, 12345, TODAY_MILLIS);
        final Posn posn = unrest.getPosn(MARAYL, EURUSD, SETTL_DAY, PARAMS_NONE, TODAY_MILLIS);
        assertPosn(MARAYL, EURUSD_MAR14, EURUSD, SETTL_DAY, 10, 123450, 10, 123450, posn);
        try {
            unrest.getPosn(MARAYL, USDJPY, SETTL_DAY, PARAMS_NONE, TODAY_MILLIS);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }
}

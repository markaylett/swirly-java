/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.rest.BackUnrest.TransStruct;

public final class OrderRestTest extends RestTest {

    @Test
    public final void testGetOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        final Map<Long, Order> out = unrest.getOrder("MARAYL", PARAMS_NONE, NOW);
        assertOrder("MARAYL", "EURUSD.MAR14", State.NEW, Side.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.get(Long.valueOf(1)));
    }

    @Test
    public final void testGetOrderMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        final Map<Long, Order> out = unrest.getOrder("MARAYL", "EURUSD.MAR14", PARAMS_NONE, NOW);
        assertOrder("MARAYL", "EURUSD.MAR14", State.NEW, Side.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.get(Long.valueOf(1)));
        assertTrue(unrest.getOrder("MARAYL", "USDJPY.MAR14", PARAMS_NONE, NOW).isEmpty());
    }

    @Test
    public final void testGetOrderMarketId() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        final Order out = unrest.getOrder("MARAYL", "EURUSD.MAR14", 1, PARAMS_NONE, NOW);
        assertOrder("MARAYL", "EURUSD.MAR14", State.NEW, Side.SELL, 12345, 10, 10, 0, 0, 0, 0, out);
        try {
            unrest.getOrder("MARAYL", "EURUSD.MAR14", 2, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Test
    public final void testDeleteOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        putOrder("MARAYL", "EURUSD.MAR14", 1, 0);
        assertNotNull(unrest.getOrder("MARAYL", "EURUSD.MAR14", 1, PARAMS_NONE, NOW));
        deleteOrder("MARAYL", "EURUSD.MAR14", 1);
        try {
            unrest.getOrder("MARAYL", "EURUSD.MAR14", 1, PARAMS_NONE, NOW);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
        try {
            deleteOrder("MARAYL", "EURUSD.MAR14", 1);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }

    @Ignore("not implemented")
    @Test
    public final void testDeleteOrderList() {
        // FIXME
    }

    @Test
    public final void testPostOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final TransStruct out = postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        assertOrder("MARAYL", "EURUSD.MAR14", State.NEW, Side.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
    }

    @Test
    public final void testPutOrder() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        TransStruct out = postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 12345, 10);
        assertOrder("MARAYL", "EURUSD.MAR14", State.NEW, Side.SELL, 12345, 10, 10, 0, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
        out = putOrder("MARAYL", "EURUSD.MAR14", 1, 5);
        assertOrder("MARAYL", "EURUSD.MAR14", State.REVISE, Side.SELL, 12345, 5, 5, 0, 0, 0, 0,
                out.orders.get(Long.valueOf(1)));
    }

    @Test
    public final void testPutOrderList() {
        // FIXME
    }
}

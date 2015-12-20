/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.swirly.domain.Role;
import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.domain.State;
import com.swirlycloud.swirly.entity.EntitySet;
import com.swirlycloud.swirly.entity.RecType;
import com.swirlycloud.swirly.entity.Trader;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.rest.BackUnrest.PosnKey;
import com.swirlycloud.swirly.rest.BackUnrest.SessStruct;

public final class TraderRestTest extends RestTest {

    // Find Trader.

    @Test
    public final void testFindByEmail() {

        String trader = unrest.findTraderByEmail("mark.aylett@gmail.com");
        assertEquals(MARAYL, trader);

        trader = unrest.findTraderByEmail("mark.aylett@gmail.comx");
        assertNull(trader);
    }

    // Get Trader.

    @Test
    public final void testGetAll() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.SELL, 10, 12345, TODAY_MILLIS);
        postOrder(MARAYL, EURUSD_MAR14, 0, Side.BUY, 10, 12345, TODAY_MILLIS);
        EntitySet es = new EntitySet(EntitySet.ORDER | EntitySet.TRADE | EntitySet.POSN);
        final SessStruct out = unrest.getSess(MARAYL, es, PARAMS_NONE, TODAY_MILLIS);
        assertOrder(MARAYL, EURUSD_MAR14, State.TRADE, Side.SELL, 10, 12345, 0, 10, 123450, 10,
                12345, out.orders.get(Long.valueOf(1)));
        assertOrder(MARAYL, EURUSD_MAR14, State.TRADE, Side.BUY, 10, 12345, 0, 10, 123450, 10,
                12345, out.orders.get(Long.valueOf(2)));
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.SELL, 10, 12345, 0, 10, 123450, 10,
                12345, EURUSD, SETTL_DAY, Role.MAKER, MARAYL, out.trades.get(Long.valueOf(3)));
        assertExec(MARAYL, EURUSD_MAR14, State.TRADE, Side.BUY, 10, 12345, 0, 10, 123450, 10, 12345,
                EURUSD, SETTL_DAY, Role.TAKER, MARAYL, out.trades.get(Long.valueOf(4)));
        assertPosn(MARAYL, EURUSD_MAR14, EURUSD, SETTL_DAY, 10, 123450, 10, 123450,
                out.posns.get(new PosnKey(EURUSD, SETTL_DAY)));
    }

    // Create Trader.

    @Test
    public final void testCreate() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {

        Trader trader = postTrader("MARAYL2", "Mark Aylett", "mark.aylett@swirlycloud.com");
        for (int i = 0; i < 2; ++i) {
            assertNotNull(trader);
            assertEquals("MARAYL2", trader.getMnem());
            assertEquals("Mark Aylett", trader.getDisplay());
            assertEquals("mark.aylett@swirlycloud.com", trader.getEmail());
            trader = (Trader) unrest.getRec(RecType.TRADER, "MARAYL2", PARAMS_NONE, TODAY_MILLIS);
        }
    }

    @Test(expected = BadRequestException.class)
    public final void testCreateDupMnem() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postTrader(MARAYL, "Mark Aylett", "mark.aylett@swirlycloud.com");
    }

    @Test(expected = BadRequestException.class)
    public final void testCreateDupEmail() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postTrader("MARAYL2", "Mark Aylett", "mark.aylett@gmail.com");
    }

    // Update Trader.

    @Test
    public final void testUpdate() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {

        Trader trader = postTrader("MARAYL2", "Mark Aylett", "mark.aylett@swirlycloud.com");
        trader = putTrader("MARAYL2", "Mark Aylettx", "mark.aylett@swirlycloud.com");
        assertNotNull(trader);
        assertEquals("MARAYL2", trader.getMnem());
        assertEquals("Mark Aylettx", trader.getDisplay());
        assertEquals("mark.aylett@swirlycloud.com", trader.getEmail());
    }
}

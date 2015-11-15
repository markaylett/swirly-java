/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.twirly.domain.EntitySet;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.rec.RecType;
import com.swirlycloud.twirly.rec.Trader;
import com.swirlycloud.twirly.rest.BackUnrest.PosnKey;
import com.swirlycloud.twirly.rest.BackUnrest.SessStruct;

public final class TraderRestTest extends RestTest {

    // Find Trader.

    @Test
    public final void testFindByEmail() {

        String trader = unrest.findTraderByEmail("mark.aylett@gmail.com");
        assertEquals("MARAYL", trader);

        trader = unrest.findTraderByEmail("mark.aylett@gmail.comx");
        assertNull(trader);
    }

    // Get Trader.

    @Test
    public final void testGetAll() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postOrder("MARAYL", "EURUSD.MAR14", Side.SELL, 10, 12345);
        postOrder("MARAYL", "EURUSD.MAR14", Side.BUY, 10, 12345);
        EntitySet es = new EntitySet(EntitySet.ORDER | EntitySet.TRADE | EntitySet.POSN);
        final SessStruct out = unrest.getSess("MARAYL", es, PARAMS_NONE, NOW);
        assertOrder("MARAYL", "EURUSD.MAR14", State.TRADE, Side.SELL, 10, 12345, 0, 10, 123450, 10,
                12345, out.orders.get(Long.valueOf(1)));
        assertOrder("MARAYL", "EURUSD.MAR14", State.TRADE, Side.BUY, 10, 12345, 0, 10, 123450, 10,
                12345, out.orders.get(Long.valueOf(2)));
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.SELL, 10, 12345, 0, 10, 123450, 10,
                12345, "EURUSD", SETTL_DAY, Role.MAKER, "MARAYL", out.trades.get(Long.valueOf(3)));
        assertExec("MARAYL", "EURUSD.MAR14", State.TRADE, Side.BUY, 10, 12345, 0, 10, 123450, 10,
                12345, "EURUSD", SETTL_DAY, Role.TAKER, "MARAYL", out.trades.get(Long.valueOf(4)));
        assertPosn("MARAYL", "EURUSD.MAR14", "EURUSD", SETTL_DAY, 10, 123450, 10, 123450,
                out.posns.get(new PosnKey("EURUSD", SETTL_DAY)));
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
            trader = (Trader) unrest.getRec(RecType.TRADER, "MARAYL2", PARAMS_NONE, NOW);
        }
    }

    @Test(expected = BadRequestException.class)
    public final void testCreateDupMnem() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        postTrader("MARAYL", "Mark Aylett", "mark.aylett@swirlycloud.com");
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

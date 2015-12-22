/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.swirlycloud.swirly.domain.RecType;
import com.swirlycloud.swirly.entity.Market;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;

public final class MarketRestTest extends RestTest {

    // Get Market.

    // Create Market.

    @Test
    public final void testCreateWithSettl() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final Market market = postMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", SETTL_DAY,
                EXPIRY_DAY, 0x1);
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", SETTL_DAY, EXPIRY_DAY, 0x1,
                market);
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", SETTL_DAY, EXPIRY_DAY, 0x1,
                (Market) unrest.getRec(RecType.MARKET, "GBPUSD.MAR14", PARAMS_NONE, TODAY_MILLIS));
    }

    @Test
    public final void testCreateWithoutSettl() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final Market market = postMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", 0x1);
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", 0, 0, 0x1, market);
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", 0, 0, 0x1,
                (Market) unrest.getRec(RecType.MARKET, "GBPUSD.MAR14", PARAMS_NONE, TODAY_MILLIS));
    }

    // Update Market.

    @Test
    public final void testUpdate() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        Market market = postMarket("GBPUSD.MAR14", "GBPUSD March 14", "GBPUSD", SETTL_DAY,
                EXPIRY_DAY, 0x1);
        market = putMarket(MARAYL, "GBPUSD.MAR14", "GBPUSD March 14x", 0x2);
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14x", "GBPUSD", SETTL_DAY, EXPIRY_DAY, 0x2,
                market);
        assertMarket("GBPUSD.MAR14", "GBPUSD March 14x", "GBPUSD", SETTL_DAY, EXPIRY_DAY, 0x2,
                (Market) unrest.getRec(RecType.MARKET, "GBPUSD.MAR14", PARAMS_NONE, TODAY_MILLIS));
    }

    // End-of-day.

    @Ignore("not implemented")
    @Test
    public final void testEndOfDay() {
        // FIXME
    }
}

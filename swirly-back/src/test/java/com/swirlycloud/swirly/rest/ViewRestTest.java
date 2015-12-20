/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_EXPIRED;
import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.swirlycloud.swirly.entity.MarketView;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;

public final class ViewRestTest extends RestTest {

    // Get View.

    @Test
    public final void testGetAll() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {

        long now = TODAY_MILLIS;

        Map<String, MarketView> views = unrest.getView(PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertEquals(2, views.size());
            MarketView view = views.get(EURUSD_MAR14);
            assertView(EURUSD_MAR14, EURUSD, SETTL_DAY, view);
            view = views.get(USDJPY_MAR14);
            assertView(USDJPY_MAR14, USDJPY, SETTL_DAY, view);
            // Use now beyond expiry.
            now = jdToMillis(EXPIRY_DAY + 1);
            views = unrest.getView(PARAMS_EXPIRED, now);
        }

        views = unrest.getView(PARAMS_NONE, now);
        assertTrue(views.isEmpty());
    }

    @Test
    public final void testGetByMarket() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {

        long now = TODAY_MILLIS;

        try {
            unrest.getView("USDCHF.MAR14", PARAMS_NONE, now);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        MarketView view = unrest.getView(EURUSD_MAR14, PARAMS_NONE, now);
        for (int i = 0; i < 2; ++i) {
            assertView(EURUSD_MAR14, EURUSD, SETTL_DAY, view);
            // Use now beyond expiry.
            now = jdToMillis(EXPIRY_DAY + 1);
            view = unrest.getView(EURUSD_MAR14, PARAMS_EXPIRED, now);
        }
    }
}

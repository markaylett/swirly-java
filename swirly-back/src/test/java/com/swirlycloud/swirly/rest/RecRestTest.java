/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.swirlycloud.swirly.entity.Asset;
import com.swirlycloud.swirly.entity.Contr;
import com.swirlycloud.swirly.entity.EntitySet;
import com.swirlycloud.swirly.entity.Market;
import com.swirlycloud.swirly.entity.Rec;
import com.swirlycloud.swirly.entity.RecType;
import com.swirlycloud.swirly.entity.Trader;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.rest.BackUnrest.RecStruct;

public final class RecRestTest extends RestTest {

    // Get Rec.

    @Test
    public final void testGetAll()
            throws NotFoundException, ServiceUnavailableException, IOException {

        // With traders.
        EntitySet es = new EntitySet(
                EntitySet.ASSET | EntitySet.CONTR | EntitySet.MARKET | EntitySet.TRADER);
        RecStruct st = unrest.getRec(es, PARAMS_NONE, TODAY_MILLIS);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertEquals(2, st.markets.size());
        assertMarket(EURUSD_MAR14, "EURUSD March 14", EURUSD, SETTL_DAY, EXPIRY_DAY, 0x1,
                st.markets.get(EURUSD_MAR14));
        assertMarket(USDJPY_MAR14, "USDJPY March 14", USDJPY, SETTL_DAY, EXPIRY_DAY, 0x1,
                st.markets.get(USDJPY_MAR14));
        assertTraders(st.traders);

        // Without traders.
        es = new EntitySet(EntitySet.ASSET | EntitySet.CONTR | EntitySet.MARKET);
        st = unrest.getRec(es, PARAMS_NONE, TODAY_MILLIS);
        assertAssets(st.assets);
        assertContrs(st.contrs);
        assertEquals(2, st.markets.size());
        assertMarket(EURUSD_MAR14, "EURUSD March 14", EURUSD, SETTL_DAY, EXPIRY_DAY, 0x1,
                st.markets.get(EURUSD_MAR14));
        assertMarket(USDJPY_MAR14, "USDJPY March 14", USDJPY, SETTL_DAY, EXPIRY_DAY, 0x1,
                st.markets.get(USDJPY_MAR14));
        assertTrue(st.traders.isEmpty());
    }

    @Test
    public final void testGetByRecType()
            throws NotFoundException, ServiceUnavailableException, IOException {

        Map<String, Rec> recs = unrest.getRec(RecType.ASSET, PARAMS_NONE, TODAY_MILLIS);
        assertAssets(recs);

        recs = unrest.getRec(RecType.CONTR, PARAMS_NONE, TODAY_MILLIS);
        assertContrs(recs);

        recs = unrest.getRec(RecType.MARKET, PARAMS_NONE, TODAY_MILLIS);
        assertEquals(2, recs.size());
        assertMarket(EURUSD_MAR14, "EURUSD March 14", EURUSD, SETTL_DAY, EXPIRY_DAY, 0x1,
                (Market) recs.get(EURUSD_MAR14));
        assertMarket(USDJPY_MAR14, "USDJPY March 14", USDJPY, SETTL_DAY, EXPIRY_DAY, 0x1,
                (Market) recs.get(USDJPY_MAR14));

        recs = unrest.getRec(RecType.TRADER, PARAMS_NONE, TODAY_MILLIS);
        assertTraders(recs);
    }

    @Test
    public final void testGetByTypeMnem()
            throws NotFoundException, ServiceUnavailableException, IOException {

        final Asset asset = (Asset) unrest.getRec(RecType.ASSET, "JPY", PARAMS_NONE, TODAY_MILLIS);
        assertAsset("JPY", asset);
        try {
            unrest.getRec(RecType.ASSET, "JPYx", PARAMS_NONE, TODAY_MILLIS);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        final Contr contr = (Contr) unrest.getRec(RecType.CONTR, USDJPY, PARAMS_NONE, TODAY_MILLIS);
        assertContr(USDJPY, contr);
        try {
            unrest.getRec(RecType.CONTR, "USDJPYx", PARAMS_NONE, TODAY_MILLIS);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        final Market market = (Market) unrest.getRec(RecType.MARKET, EURUSD_MAR14, PARAMS_NONE,
                TODAY_MILLIS);
        assertMarket(EURUSD_MAR14, "EURUSD March 14", EURUSD, SETTL_DAY, EXPIRY_DAY, 0x1, market);
        try {
            unrest.getRec(RecType.MARKET, "EURUSD.MAR14x", PARAMS_NONE, TODAY_MILLIS);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }

        final Trader trader = (Trader) unrest.getRec(RecType.TRADER, MARAYL, PARAMS_NONE,
                TODAY_MILLIS);
        assertTrader(MARAYL, trader);
        try {
            unrest.getRec(RecType.TRADER, "MARAYLx", PARAMS_NONE, TODAY_MILLIS);
            fail("Expected exception");
        } catch (final NotFoundException e) {
        }
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.mock.MockContr;

public final class ContrTest {
    @Test
    public final void test() {
        assertEquals(
                "{\"mnem\":\"EURUSD\",\"display\":\"EURUSD\",\"assetType\":\"CURRENCY\",\"asset\":\"EUR\",\"ccy\":\"USD\",\"tickNumer\":1,\"tickDenom\":10000,\"lotNumer\":1000000,\"lotDenom\":1,\"priceDp\":4,\"pipDp\":4,\"qtyDp\":0,\"minLots\":1,\"maxLots\":10}",
                MockContr.newContr("EURUSD").toString());
    }
}

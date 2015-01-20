/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.twirly.mock.MockContr;

public final class ContrTest {
    @Test
    public final void testToString() {
        assertEquals(
                "{\"id\":12,\"mnem\":\"EURUSD\",\"display\":\"EURUSD\",\"assetType\":\"CURRENCY\",\"asset\":\"EUR\",\"ccy\":\"USD\",\"tickNumer\":1,\"tickDenom\":10000,\"lotNumer\":1000000,\"lotDenom\":1,\"pipDp\":4,\"minLots\":1,\"maxLots\":10}",
                MockContr.newContr("EURUSD").toString());
    }
}

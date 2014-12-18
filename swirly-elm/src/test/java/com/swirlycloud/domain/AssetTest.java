/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.mock.MockAsset;

public final class AssetTest {
    @Test
    public final void test() {
        assertEquals(
                "{\"mnem\":\"GBP\",\"display\":\"United Kingdom, Pounds\",\"type\":\"CURRENCY\"}",
                MockAsset.newAsset("GBP").toString());
    }
}

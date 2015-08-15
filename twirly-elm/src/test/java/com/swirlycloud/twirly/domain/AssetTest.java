/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.mock.MockAsset;

public final class AssetTest extends SerializableTest {
    private static final Factory FACTORY = new BasicFactory();
    private static final MockAsset MOCK_ASSET = new MockAsset(FACTORY);

    @Test
    public final void testToString() {
        assertEquals(
                "{\"mnem\":\"GBP\",\"display\":\"United Kingdom, Pounds\",\"type\":\"CURRENCY\"}",
                MOCK_ASSET.newAsset("GBP").toString());
    }

    @Test
    public final void testSerializable() throws ClassNotFoundException, IOException {
        final MnemRbTree t = MOCK_ASSET.selectAsset();
        final MnemRbTree u = writeAndRead(t);

        assertEquals(toJsonString(t.getFirst()), toJsonString(u.getFirst()));
    }
}

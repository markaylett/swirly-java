/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.mock.MockTrader;

public final class TraderTest extends SerializableTest {

    private static final Factory FACTORY = new BasicFactory();
    private static final MockTrader MOCK_TRADER = new MockTrader(FACTORY);

    @Test
    public final void testToString() {
        assertEquals(
                "{\"mnem\":\"MARAYL\",\"display\":\"Mark Aylett\",\"email\":\"mark.aylett@gmail.com\"}",
                MOCK_TRADER.newTrader("MARAYL").toString());
    }

    @Test
    public final void testSerializable() throws ClassNotFoundException, IOException {
        final MnemRbTree t = MOCK_TRADER.selectTrader();
        final MnemRbTree u = writeAndRead(t);

        assertEquals(toJsonString(t.getFirst()), toJsonString(u.getFirst()));
    }
}

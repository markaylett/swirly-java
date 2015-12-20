/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.swirly.entity.BasicFactory;
import com.swirlycloud.swirly.entity.Factory;
import com.swirlycloud.swirly.entity.RecTree;
import com.swirlycloud.swirly.mock.MockTrader;

public final class TraderTest extends SerializableTest {

    private static final Factory FACTORY = new BasicFactory();

    @Test
    public final void testToString() {
        assertEquals(
                "{\"mnem\":\"MARAYL\",\"display\":\"Mark Aylett\",\"email\":\"mark.aylett@gmail.com\"}",
                MockTrader.newTrader("MARAYL", FACTORY).toString());
    }

    @Test
    public final void testSerializable() throws ClassNotFoundException, IOException {
        final RecTree t = MockTrader.readTrader(FACTORY);
        final RecTree u = writeAndRead(t);

        assertEquals(toJsonString(t.getFirst()), toJsonString(u.getFirst()));
    }
}

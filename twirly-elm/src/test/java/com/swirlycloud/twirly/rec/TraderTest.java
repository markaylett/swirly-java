/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rec;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.twirly.domain.BasicFactory;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.SerializableTest;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.mock.MockTrader;

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
        final MnemRbTree t = MockTrader.readTrader(FACTORY);
        final MnemRbTree u = writeAndRead(t);

        assertEquals(toJsonString(t.getFirst()), toJsonString(u.getFirst()));
    }
}

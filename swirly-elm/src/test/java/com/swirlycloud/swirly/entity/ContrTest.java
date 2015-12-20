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
import com.swirlycloud.swirly.mock.MockContr;

public final class ContrTest extends SerializableTest {

    private static final Factory FACTORY = new BasicFactory();

    @Test
    public final void testToString() {
        assertEquals(
                "{\"mnem\":\"EURUSD\",\"display\":\"EURUSD\",\"asset\":\"EUR\",\"ccy\":\"USD\",\"lotNumer\":1000000,\"lotDenom\":1,\"tickNumer\":1,\"tickDenom\":10000,\"pipDp\":4,\"minLots\":1,\"maxLots\":10}",
                MockContr.newContr("EURUSD", FACTORY).toString());
    }

    @Test
    public final void testSerializable() throws ClassNotFoundException, IOException {
        final RecTree t = MockContr.readContr(FACTORY);
        final RecTree u = writeAndRead(t);

        assertEquals(toJsonString(t.getFirst()), toJsonString(u.getFirst()));
    }
}

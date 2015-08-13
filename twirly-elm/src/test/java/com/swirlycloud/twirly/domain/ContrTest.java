/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.mock.MockContr;

public final class ContrTest extends SerializableTest {

    private static final Factory FACTORY = new BasicFactory();
    private static final MockContr MOCK_CONTR = new MockContr(FACTORY);

    @Test
    public final void testToString() {
        assertEquals(
                "{\"mnem\":\"EURUSD\",\"display\":\"EURUSD\",\"asset\":\"EUR\",\"ccy\":\"USD\",\"tickNumer\":1,\"tickDenom\":10000,\"lotNumer\":1000000,\"lotDenom\":1,\"pipDp\":4,\"minLots\":1,\"maxLots\":10}",
                MOCK_CONTR.newContr("EURUSD").toString());
    }

    @Test
    public final void testSerializable() throws ClassNotFoundException, IOException {
        final MnemRbTree t = MOCK_CONTR.selectContr();
        final MnemRbTree u = writeAndRead(t);

        assertEquals(toJsonString(t.getFirst()), toJsonString(u.getFirst()));
    }
}

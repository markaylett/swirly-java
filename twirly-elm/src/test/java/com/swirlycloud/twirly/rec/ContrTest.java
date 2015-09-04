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
import com.swirlycloud.twirly.mock.MockContr;

public final class ContrTest extends SerializableTest {

    private static final Factory FACTORY = new BasicFactory();

    @Test
    public final void testToString() {
        assertEquals(
                "{\"mnem\":\"EURUSD\",\"display\":\"EURUSD\",\"asset\":\"EUR\",\"ccy\":\"USD\",\"tickNumer\":1,\"tickDenom\":10000,\"lotNumer\":1000000,\"lotDenom\":1,\"pipDp\":4,\"minLots\":1,\"maxLots\":10}",
                MockContr.newContr("EURUSD", FACTORY).toString());
    }

    @Test
    public final void testSerializable() throws ClassNotFoundException, IOException {
        final MnemRbTree t = MockContr.selectContr(FACTORY);
        final MnemRbTree u = writeAndRead(t);

        assertEquals(toJsonString(t.getFirst()), toJsonString(u.getFirst()));
    }
}

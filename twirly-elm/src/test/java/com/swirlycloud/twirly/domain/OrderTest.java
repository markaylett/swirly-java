/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.mock.MockTrader;

public final class OrderTest {
    @Test
    public final void testToString() {
        final Order order = new Order(1, MockTrader.newTrader("MARAYL"),
                MockContr.newContr("EURUSD"), ymdToJd(2014, 2, 14), "test", Action.BUY, 12345, 2,
                1, 1414692516006L);
        assertEquals(
                "{\"id\":1,\"trader\":1,\"contr\":12,\"settlDate\":20140314,\"ref\":\"test\",\"state\":\"NEW\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":2,\"resd\":2,\"exec\":0,\"lastTicks\":null,\"lastLots\":null,\"minLots\":1,\"created\":1414692516006,\"modified\":1414692516006}",
                order.toString());
    }
}

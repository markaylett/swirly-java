/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.twirly.date.JulianDay;

public final class OrderTest {
    @Test
    public final void testToString() {
        final Order order = new Order(1, "MARAYL", "EURUSD.MAR14", "EURUSD",
                JulianDay.isoToJd(20140314), "test", Action.BUY, 12345, 2, 1, 1414692516006L);
        assertEquals(
                "{\"id\":1,\"trader\":\"MARAYL\",\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"ref\":\"test\",\"state\":\"NEW\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":2,\"resd\":2,\"exec\":0,\"lastTicks\":null,\"lastLots\":null,\"minLots\":1,\"created\":1414692516006,\"modified\":1414692516006}",
                order.toString());
    }
}

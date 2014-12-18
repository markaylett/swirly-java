/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockTrader;

public final class ExecTest {
    @Test
    public final void test() {
        final Order order = new Order(1, MockTrader.newTrader("MARAYL"),
                MockContr.newContr("EURUSD"), ymdToJd(2014, 2, 14), "test", Action.BUY, 12345, 3,
                1, 1414692516006L);
        final Exec exec = new Exec(2, order, 1414692516007L);
        assertEquals(
                "{\"id\":2,\"orderId\":1,\"trader\":\"MARAYL\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"ref\":\"test\",\"state\":\"NEW\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":3,\"resd\":3,\"exec\":0,\"lastTicks\":0,\"lastLots\":0,\"minLots\":1,\"created\":1414692516007}",
                exec.toString());
        exec.trade(12345, 1, 3, Role.MAKER, MockTrader.newTrader("GOSAYL"));
        assertEquals(
                "{\"id\":2,\"orderId\":1,\"trader\":\"MARAYL\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"ref\":\"test\",\"state\":\"TRADE\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":3,\"resd\":2,\"exec\":1,\"lastTicks\":12345,\"lastLots\":1,\"minLots\":1,\"matchId\":3,\"role\":\"MAKER\",\"cpty\":\"GOSAYL\",\"created\":1414692516007}",
                exec.toString());
    }
}

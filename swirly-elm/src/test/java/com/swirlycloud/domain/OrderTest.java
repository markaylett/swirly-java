/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockUser;

public final class OrderTest {
    @Test
    public final void test() {
        final Order order = new Order(1, MockUser.newUser("MARAYL"), MockContr.newContr("EURUSD"),
                ymdToJd(2014, 3, 14), "test", Action.BUY, 12345, 2, 1, 1414692516006L);
        assertEquals(
                "{\"id\":1,\"user\":\"MARAYL\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"ref\":\"test\",\"state\":\"NEW\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":2,\"resd\":2,\"exec\":0,\"lastTicks\":null,\"lastLots\":null,\"minLots\":1,\"created\":1414692516006,\"modified\":1414692516006}",
                order.toString());
    }
}

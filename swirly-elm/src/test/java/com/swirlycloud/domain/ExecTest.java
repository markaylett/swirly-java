/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Role;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockUser;

public final class ExecTest {
    @Test
    public final void test() {
        final Order order = new Order(1, MockUser.newUser("MARAYL"),
                MockContr.newContr("EURUSD"), ymdToJd(2014, 3, 14), "test", Action.BUY, 12345, 3,
                1, 1414692516006L);
        final Exec exec = new Exec(2, 1, order, 1414692516007L);
        assertEquals(
                "{\"id\":2,\"orderId\":1,\"user\":\"MARAYL\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"ref\":\"test\",\"state\":\"NEW\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":3,\"resd\":3,\"exec\":0,\"lastTicks\":0,\"lastLots\":0,\"minLots\":1,\"created\":1414692516007}",
                exec.toString());
        exec.trade(12345, 1, 3, Role.MAKER, MockUser.newUser("GOSAYL"));
        assertEquals(
                "{\"id\":2,\"orderId\":1,\"user\":\"MARAYL\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"ref\":\"test\",\"state\":\"TRADE\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":3,\"resd\":2,\"exec\":1,\"lastTicks\":12345,\"lastLots\":1,\"minLots\":1,\"matchId\":3,\"role\":\"MAKER\",\"cpty\":\"GOSAYL\",\"created\":1414692516007}",
                exec.toString());
    }
}
/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.domain.Posn;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockUser;

public final class PosnTest {
    @Test
    public final void test() {
        final Posn posn = new Posn(MockUser.newUser("MARAYL"), MockContr.newContr("EURUSD"),
                ymdToJd(2014, 3, 14));
        posn.setBuyLicks(1);
        posn.setBuyLots(2);
        posn.setSellLicks(3);
        posn.setSellLots(4);
        assertEquals(
                "{\"id\":1099512430939,\"user\":\"MARAYL\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"buyLicks\":1,\"buyLots\":2,\"sellLicks\":3,\"sellLots\":4}",
                posn.toString());
    }
}

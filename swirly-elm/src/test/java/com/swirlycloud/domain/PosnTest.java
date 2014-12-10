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
import com.swirlycloud.mock.MockTrader;

public final class PosnTest {
    @Test
    public final void test() {
        final Posn posn = new Posn(MockTrader.newTrader("MARAYL"), MockContr.newContr("EURUSD"),
                ymdToJd(2014, 3, 14));
        posn.setBuyLicks(1);
        posn.setBuyLots(2);
        posn.setSellLicks(3);
        posn.setSellLots(4);
        assertEquals(
                "{\"id\":3449558818357249,\"trader\":\"MARAYL\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"buyLicks\":1,\"buyLots\":2,\"sellLicks\":3,\"sellLots\":4}",
                posn.toString());
    }
}

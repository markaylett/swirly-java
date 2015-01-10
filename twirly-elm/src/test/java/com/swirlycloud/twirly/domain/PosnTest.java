/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.mock.MockTrader;

public final class PosnTest {
    @Test
    public final void testToString() {
        final Posn posn = new Posn(MockTrader.newTrader("MARAYL"), MockContr.newContr("EURUSD"),
                ymdToJd(2014, 2, 14));
        posn.setBuyLicks(1);
        posn.setBuyLots(2);
        posn.setSellLicks(3);
        posn.setSellLots(4);
        assertEquals(
                "{\"id\":3449558818357249,\"trader\":1,\"contr\":12,\"settlDate\":20140314,\"buyLicks\":1,\"buyLots\":2,\"sellLicks\":3,\"sellLots\":4}",
                posn.toString());
    }
}

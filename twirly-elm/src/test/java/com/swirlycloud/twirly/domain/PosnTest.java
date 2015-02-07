/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.twirly.date.JulianDay;

public final class PosnTest {
    @Test
    public final void testToString() {
        final Posn posn = new Posn("MARAYL", "EURUSD.MAR14", "EURUSD", JulianDay.isoToJd(20140314));
        posn.setBuyCost(1);
        posn.setBuyLots(2);
        posn.setSellCost(3);
        posn.setSellLots(4);
        assertEquals(
                "{\"trader\":\"MARAYL\",\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"buyCost\":1,\"buyLots\":2,\"sellCost\":3,\"sellLots\":4}",
                posn.toString());
    }
}

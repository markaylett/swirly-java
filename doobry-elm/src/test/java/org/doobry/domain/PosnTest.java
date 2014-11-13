/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import static org.doobry.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.doobry.mock.MockContr;
import org.doobry.mock.MockUser;
import org.junit.Test;

public final class PosnTest {
    @Test
    public final void test() {
        final Posn posn = new Posn(MockUser.newUser("WRAMIREZ"), MockContr.newContr("EURUSD"),
                ymdToJd(2014, 3, 14));
        posn.setBuyLicks(1);
        posn.setBuyLots(2);
        posn.setSellLicks(3);
        posn.setSellLots(4);
        assertEquals(
                "{\"id\":10995117080923,\"user\":\"WRAMIREZ\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"buyLicks\":1,\"buyLots\":2,\"sellLicks\":3,\"sellLots\":4}",
                posn.toString());
    }
}

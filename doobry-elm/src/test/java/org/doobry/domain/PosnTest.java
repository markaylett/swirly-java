/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import static org.doobry.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.doobry.mock.MockContr;
import org.doobry.mock.MockParty;
import org.junit.Test;

public final class PosnTest {
    @Test
    public final void test() {
        final Posn posn = new Posn(MockParty.newParty("DBRA"), MockContr.newContr("EURUSD"),
                ymdToJd(2014, 3, 14));
        posn.setBuyLicks(1);
        posn.setBuyLots(2);
        posn.setSellLicks(3);
        posn.setSellLots(4);
        assertEquals(
                "{\"accnt\":\"DBRA\",\"contr\":\"EURUSD\",\"settl_date\":20140314,\"buy_licks\":1,\"buy_lots\":2,\"sell_licks\":3,\"sell_lots\":4}",
                posn.toString());
    }
}

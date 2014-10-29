/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import static org.doobry.util.Date.ymdToJd;

import org.doobry.domain.Action;
import org.doobry.domain.Reg;
import org.doobry.mock.MockBank;
import org.doobry.mock.MockJourn;
import org.doobry.mock.MockModel;
import org.junit.Test;

public final class ServTest {
    @Test
    public final void test() {
        final Serv s = new Serv(new MockBank(Reg.values().length), new MockJourn());
        try {
            s.load(new MockModel());
            final Accnt trader = s.getLazyAccnt("WRAMIREZ");
            final Accnt giveup = s.getLazyAccnt("DBRA");
            final Book book = s.getLazyBook("EURUSD", ymdToJd(2014, 3, 14));
            s.placeOrder(trader, giveup, book, null, Action.BUY, 12345, 1, 1);
        } finally {
            s.close();
        }
    }
}

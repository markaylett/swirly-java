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

public final class BookTest {
    @Test
    public final void test() {
        final Contr contr = MockContr.newContr("EURUSD");
        final int settlDay = ymdToJd(2014, 3, 14);
        final Book book = new Book(contr, settlDay);

        final User user = MockUser.newUser("WRAMIREZ");
        final long now = 1414932078620L;

        book.placeOrder(
                new Order(1, user, contr, settlDay, "apple", Action.BUY, 12343, 10, 0, now), now);
        book.placeOrder(
                new Order(1, user, contr, settlDay, "orange", Action.BUY, 12344, 5, 0, now), now);
        book.placeOrder(new Order(1, user, contr, settlDay, "pear", Action.SELL, 12346, 5, 0, now),
                now);
        book.placeOrder(
                new Order(1, user, contr, settlDay, "banana", Action.SELL, 12346, 2, 0, now), now);

        final StringBuilder sb = new StringBuilder();
        book.print(sb);
        assertEquals(
                "{\"contr\":\"EURUSD\",\"settlDate\":20140314,\"bidTicks\":[12344,12343,0,0,0],\"bidLots\":[5,10,0,0,0],\"bidCount\":[1,1,0,0,0],\"offerTicks\":[12346,0,0,0,0],\"offerLots\":[7,0,0,0,0],\"offerCount\":[2,0,0,0,0]}",
                sb.toString());
    }
}

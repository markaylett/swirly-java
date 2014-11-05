/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import static org.doobry.util.Date.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.doobry.domain.Action;
import org.doobry.domain.Contr;
import org.doobry.domain.Order;
import org.doobry.domain.User;
import org.doobry.mock.MockContr;
import org.doobry.mock.MockUser;
import org.junit.Test;

public final class ViewTest {
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
        View.print(sb, book, now);
        assertEquals(
                "{\"contr\":\"EURUSD\",\"settl_date\":20140314,\"bid_ticks\":[12344,12343,0],\"bid_lots\":[5,10,0],\"bid_count\":[1,1,0],\"offer_ticks\":[12346,0,0],\"offer_lots\":[7,0,0],\"offer_count\":[2,0,0],\"created\":1414932078620}",
                sb.toString());
    }
}

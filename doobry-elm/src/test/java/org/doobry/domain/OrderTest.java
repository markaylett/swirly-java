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

public final class OrderTest {
    @Test
    public final void test() {
        final Order order = new Order(1, MockParty.newParty("WRAMIREZ"),
                MockParty.newParty("DBRA"), MockContr.newContr("EURUSD"), ymdToJd(2014, 3, 14),
                "test", Action.BUY, 12345, 2, 1, 1414692516006L);
        assertEquals(
                "{\"id\":1,\"trader\":\"WRAMIREZ\",\"giveup\":\"DBRA\",\"contr\":\"EURUSD\",\"settl_date\":20140314,\"ref\":\"test\",\"state\":\"NEW\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":2,\"resd\":2,\"exec\":0,\"last_ticks\":0,\"last_lots\":0,\"created\":1414692516006,\"modified\":1414692516006}",
                order.toString());
    }
}

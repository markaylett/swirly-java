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

public final class ExecTest {
    @Test
    public final void test() {
        final Order order = new Order(1, MockParty.newParty("WRAMIREZ"),
                MockParty.newParty("DBRA"), MockContr.newContr("EURUSD"), ymdToJd(2014, 3, 14),
                "test", Action.BUY, 12345, 3, 1, 1414692516006L);
        final Exec exec = new Exec(2, 1, order, 1414692516007L);
        assertEquals(
                "{\"id\":2,\"order\":1,\"trader\":\"WRAMIREZ\",\"giveup\":\"DBRA\",\"contr\":\"EURUSD\",\"settl_date\":20140314,\"ref\":\"test\",\"state\":\"NEW\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":3,\"resd\":3,\"exec\":0,\"last_ticks\":0,\"last_lots\":0,\"created\":1414692516007}",
                exec.toString());
        exec.trade(12345, 1, 3, Role.MAKER, MockParty.newParty("BJONES"));
        assertEquals(
                "{\"id\":2,\"order\":1,\"trader\":\"WRAMIREZ\",\"giveup\":\"DBRA\",\"contr\":\"EURUSD\",\"settl_date\":20140314,\"ref\":\"test\",\"state\":\"TRADE\",\"action\":\"BUY\",\"ticks\":12345,\"lots\":3,\"resd\":2,\"exec\":1,\"last_ticks\":12345,\"last_lots\":1,\"match\":3,\"role\":\"MAKER\",\"cpty\":\"BJONES\",\"created\":1414692516007}",
                exec.toString());
    }
}

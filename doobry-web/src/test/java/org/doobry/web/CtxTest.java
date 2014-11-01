/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import org.doobry.domain.Action;
import org.doobry.domain.RecType;
import org.junit.Test;

public class CtxTest {
    @Test
    public final void test() {
        System.out.println(Ctx.getInstance().getRec(RecType.ASSET));
        System.out.println(Ctx.getInstance().getRecByMnem(RecType.CONTR, "EURUSD"));
        System.out.println(Ctx.getInstance().postOrderByAccnt("WRAMIREZ", "DBRA", "EURUSD",
                20141031, "test", Action.BUY, 12345, 5, 1));
        System.out.println(Ctx.getInstance().getOrderByAccnt("WRAMIREZ"));
        System.out.println(Ctx.getInstance().getOrderByAccntAndId("WRAMIREZ", 1));
        System.out.println(Ctx.getInstance().putOrderByAccntAndId("WRAMIREZ", 1, 4));
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.junit.Assert.assertEquals;

import org.doobry.domain.Action;
import org.junit.Test;

public final class CtxTest {
    @Test
    public final void test() {
        final StringBuilder expected = new StringBuilder();
        Ctx.getInstance().postOrder(expected, "WRAMIREZ", "EURUSD", 20141031, "test", Action.BUY,
                12345, 5, 1);
        final StringBuilder actual = new StringBuilder();
        Ctx.getInstance().getOrder(actual, "WRAMIREZ", 1);
        assertEquals(expected.toString(), actual.toString());
    }
}

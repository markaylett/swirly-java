/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.junit.Assert.assertEquals;

import org.doobry.domain.Action;
import org.junit.Test;

public final class RestTest {
    @Test
    public final void testOne() {
        final Rest ctx = new Rest();
        final StringBuilder expected = new StringBuilder();
        ctx.postOrder(expected, "WRAMIREZ", "EURUSD", 20141031, "test", Action.BUY, 12345, 5, 1);
        final StringBuilder actual = new StringBuilder();
        ctx.getOrder(actual, "WRAMIREZ", 1);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public final void testAll() {
        final Rest ctx = new Rest();
        final StringBuilder expected = new StringBuilder();
        expected.append('[');
        ctx.postOrder(expected, "WRAMIREZ", "EURUSD", 20141031, "test", Action.BUY, 12345, 5, 1);
        expected.append(']');
        final StringBuilder actual = new StringBuilder();
        ctx.getOrder(actual, "WRAMIREZ");
        assertEquals(expected.toString(), actual.toString());
    }
}

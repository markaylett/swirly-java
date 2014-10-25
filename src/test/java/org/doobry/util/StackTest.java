/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

import static org.junit.Assert.*;

import org.junit.Test;

public final class StackTest {
    @Test
    public final void test() {
        final Stack s = new Stack();
        assertTrue(s.isEmpty());
        assertNull(s.getFirst());
    }
}

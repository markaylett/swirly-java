/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

import static org.junit.Assert.*;

import org.junit.Test;

public final class ListTest {
    @Test
    public final void test() {
        final List l = new List();
        assertTrue(l.isEmpty());
        assertTrue(l.getFirst().isEnd());
    }
}

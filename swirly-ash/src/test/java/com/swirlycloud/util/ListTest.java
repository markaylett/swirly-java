/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.swirlycloud.util.List;

public final class ListTest {
    @Test
    public final void test() {
        final List l = new List();
        assertTrue(l.isEmpty());
        assertTrue(l.getFirst().isEnd());
    }
}

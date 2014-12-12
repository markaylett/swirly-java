/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.collection;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.collection.List;

public final class ListTest {
    @Test
    public final void test() {
        final List l = new List();
        assertTrue(l.isEmpty());
        assertTrue(l.getFirst().isEnd());
    }
}

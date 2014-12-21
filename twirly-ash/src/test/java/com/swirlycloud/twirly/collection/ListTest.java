/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.twirly.collection.List;

public final class ListTest {
    @Test
    public final void test() {
        final List l = new List();
        assertTrue(l.isEmpty());
        assertTrue(l.getFirst().isEnd());
    }
}

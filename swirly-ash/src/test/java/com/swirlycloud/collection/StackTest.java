/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.collection;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.collection.Stack;

public final class StackTest {
    @Test
    public final void test() {
        final Stack s = new Stack();
        assertTrue(s.isEmpty());
        assertNull(s.getFirst());
    }
}

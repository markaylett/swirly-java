/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.collection;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.collection.Queue;

public final class QueueTest {
    @Test
    public final void test() {
        final Queue q = new Queue();
        assertTrue(q.isEmpty());
        assertNull(q.getFirst());
    }
}

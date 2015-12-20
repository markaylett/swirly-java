/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PriorityQueueTest {
    @Test
    public final void test() {
        final PriorityQueue<Integer> pq = new PriorityQueue<>();
        assertTrue(pq.isEmpty());
        assertNull(pq.getFirst());
        assertNull(pq.removeFirst());
        pq.add(5);
        pq.add(2);
        pq.add(3);
        pq.add(4);
        pq.add(1);
        for (int i = 1; i <= 5; ++i) {
            assertEquals(Integer.valueOf(i), pq.getFirst());
            assertEquals(Integer.valueOf(i), pq.pop());
        }
        assertTrue(pq.isEmpty());
        assertNull(pq.getFirst());
        assertNull(pq.removeFirst());
    }
}

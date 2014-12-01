/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class TreeTest {
    private static final class Node extends BasicRbNode {
        private final long key;
        private final String name;

        Node(long key, String name) {
            this.key = key;
            this.name = name;
        }

        @Override
        public final long getKey() {
            return key;
        }

        @Override
        public final String toString() {
            return name;
        }
    }

    @Test
    public final void test() {
        final Tree t = new Tree();
        final Node first = new Node(101, "first");
        final Node second = new Node(102, "second");
        final Node third = new Node(103, "third");
        assertTrue(t.isEmpty());
        assertNull(t.getFirst());
        assertNull(t.getLast());
        assertEquals(first.getColor(), 0);
        t.insert(first);
        assertNotEquals(first.getColor(), 0);
        assertFalse(t.isEmpty());
        assertSame(first, t.getFirst());
        assertSame(first, t.getLast());
        t.insert(second);
        assertSame(first, t.getFirst());
        assertSame(second, t.getLast());
        t.insert(third);
        assertSame(first, t.getFirst());
        assertSame(third, t.getLast());
        t.remove(first);
        assertEquals(first.getColor(), 0);
        assertSame(second, t.getFirst());
        assertSame(third, t.getLast());
        t.remove(second);
        t.remove(third);
        assertTrue(t.isEmpty());
        assertNull(t.getFirst());
        assertNull(t.getLast());
    }
}

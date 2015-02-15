/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.CollectionUtil.compareLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.RbNode;

public final class LongRbTreeTest {

    private static final class Node extends BasicRbNode {
        private final long key;

        public Node(long key) {
            this.key = key;
        }
    }

    private static final class NodeTree extends LongRbTree {

        private static long getKey(RbNode node) {
            return ((Node) node).key;
        }

        @Override
        protected final int compareKey(RbNode lhs, RbNode rhs) {
            return compareLong(getKey(lhs), getKey(rhs));
        }

        @Override
        protected final int compareKeyDirect(RbNode lhs, long rhs) {
            return compareLong(getKey(lhs), rhs);
        }
    }

    @Test
    public final void test() {
        final LongRbTree t = new NodeTree();
        final Node first = new Node(101);
        final Node second = new Node(102);
        final Node third = new Node(103);
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

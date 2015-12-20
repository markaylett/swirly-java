/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.intrusive;

import static com.swirlycloud.swirly.util.CollectionUtil.hashLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.swirlycloud.swirly.node.AbstractSlNode;
import com.swirlycloud.swirly.node.SlNode;

public final class AbstractSlLongMapTest {

    private static final class Node extends AbstractSlNode {
        private final long key;

        public Node(long key) {
            this.key = key;
        }
    }

    private static final class NodeMap extends AbstractSlLongMap {

        private static long getKey(SlNode node) {
            return ((Node) node).key;
        }

        @Override
        protected final int hashNode(SlNode node) {
            return hashLong(getKey(node));
        }

        @Override
        protected final boolean equalNode(SlNode lhs, SlNode rhs) {
            return getKey(lhs) == getKey(rhs);
        }

        @Override
        protected final boolean equalKey(SlNode lhs, long rhs) {
            return getKey(lhs) == rhs;
        }

        public NodeMap() {
            super(12);
        }
    }

    @Test
    public final void testEmpty() {
        final NodeMap t = new NodeMap();
        assertNull(t.remove(1));
        assertNull(t.find(1));
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
    }

    @Test
    public final void testInsert() {

        final Node one = new Node(1);
        final Node two = new Node(2);
        final Node three = new Node(3);
        final Node four = new Node(4);

        final NodeMap t = new NodeMap();
        assertNull(t.insert(one));
        assertFalse(t.isEmpty());
        assertEquals(1, t.size());
        assertNull(t.insert(two));
        assertFalse(t.isEmpty());
        assertEquals(2, t.size());
        assertNull(t.insert(three));
        assertFalse(t.isEmpty());
        assertEquals(3, t.size());
        assertNull(t.insert(four));
        assertFalse(t.isEmpty());
        assertEquals(4, t.size());
    }

    @Test
    public final void testReplace() {

        final Node one = new Node(1);
        final Node two = new Node(2);
        final Node three = new Node(3);
        final Node four = new Node(4);

        final NodeMap t = new NodeMap();
        t.insert(one);
        t.insert(two);
        assertSame(one, t.insert(new Node(1)));
        assertEquals(2, t.size());
        assertSame(two, t.insert(new Node(2)));
        assertEquals(2, t.size());

        t.insert(three);
        t.insert(four);
        assertSame(three, t.insert(new Node(3)));
        assertEquals(4, t.size());
        assertSame(four, t.insert(new Node(4)));
        assertEquals(4, t.size());
    }

    @Test
    public final void testRemove() {

        final Node one = new Node(1);
        final Node two = new Node(2);
        final Node three = new Node(3);
        final Node four = new Node(4);

        final NodeMap t = new NodeMap();
        t.insert(one);
        t.insert(two);
        t.insert(three);
        t.insert(four);

        assertSame(one, t.remove(1));
        assertEquals(3, t.size());
        assertSame(two, t.remove(2));
        assertEquals(2, t.size());
        assertSame(three, t.remove(3));
        assertEquals(1, t.size());
        assertSame(four, t.remove(4));
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
        assertNull(t.remove(5));
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
    }

    @Test
    public final void testFind() {

        final Node one = new Node(1);
        final Node two = new Node(2);
        final Node three = new Node(3);
        final Node four = new Node(4);

        final NodeMap t = new NodeMap();
        t.insert(one);
        t.insert(two);
        t.insert(three);
        t.insert(four);

        assertSame(one, t.find(1));
        assertSame(two, t.find(2));
        assertSame(three, t.find(3));
        assertSame(four, t.find(4));
        assertNull(t.find(5));
    }

    @Test
    public final void testLoad() {
        final Random r = new Random();
        final NodeMap t = new NodeMap();
        for (long i = 0; i < 100000; ++i) {
            final long l = r.nextLong();
            t.insert(new Node(l));
            assertNotNull(t.find(l));
        }
    }
}

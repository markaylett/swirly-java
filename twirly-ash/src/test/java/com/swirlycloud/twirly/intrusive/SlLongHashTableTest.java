/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.swirlycloud.twirly.node.BasicSlNode;
import com.swirlycloud.twirly.node.SlNode;

public final class SlLongHashTableTest {

    private static final class Entry extends BasicSlNode {
        final long id;
        public Entry(long id) {
            this.id = id;
        }
    }

    private static long getId(SlNode node) {
        return ((Entry) node).id;
    }

    private static final class EntryHashTable extends SlLongHashTable {

        @Override
        protected final int hashKey(SlNode node) {
            return hashKey(getId(node));
        }

        @Override
        protected final boolean equalKey(SlNode lhs, SlNode rhs) {
            return getId(lhs) == getId(rhs);
        }

        @Override
        protected final boolean equalKey(SlNode lhs, long rhs) {
            return getId(lhs) == rhs;
        }

        public EntryHashTable() {
            super(12);
        }
    }

    @Test
    public final void testEmpty() {
        final EntryHashTable t = new EntryHashTable();
        assertNull(t.remove(1));
        assertNull(t.find(1));
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
    }

    @Test
    public final void testInsert() {

        final Entry one = new Entry(1);
        final Entry two = new Entry(2);
        final Entry three = new Entry(3);
        final Entry four = new Entry(4);

        final EntryHashTable t = new EntryHashTable();
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

        final Entry one = new Entry(1);
        final Entry two = new Entry(2);
        final Entry three = new Entry(3);
        final Entry four = new Entry(4);

        final EntryHashTable t = new EntryHashTable();
        t.insert(one);
        t.insert(two);
        assertSame(one, t.insert(new Entry(1)));
        assertEquals(2, t.size());
        assertSame(two, t.insert(new Entry(2)));
        assertEquals(2, t.size());

        t.insert(three);
        t.insert(four);
        assertSame(three, t.insert(new Entry(3)));
        assertEquals(4, t.size());
        assertSame(four, t.insert(new Entry(4)));
        assertEquals(4, t.size());
    }

    @Test
    public final void testRemove() {

        final Entry one = new Entry(1);
        final Entry two = new Entry(2);
        final Entry three = new Entry(3);
        final Entry four = new Entry(4);

        final EntryHashTable t = new EntryHashTable();
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

        final Entry one = new Entry(1);
        final Entry two = new Entry(2);
        final Entry three = new Entry(3);
        final Entry four = new Entry(4);

        final EntryHashTable t = new EntryHashTable();
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
        final EntryHashTable t = new EntryHashTable();
        for (long i = 0; i < 100000; ++i) {
            final long l = r.nextLong();
            t.insert(new Entry(l));
            assertNotNull(t.find(l));
        }
    }
}

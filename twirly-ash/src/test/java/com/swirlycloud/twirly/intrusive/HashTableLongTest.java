/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.junit.Test;

import static org.junit.Assert.*;

import com.swirlycloud.twirly.node.BasicSlNode;

public final class HashTableLongTest {

    private static final class Entry extends BasicSlNode {
        transient Entry next;
        final long id;
        @SuppressWarnings("unused")
        final String name;

        public Entry(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final class EntryHashTable extends HashTableLong<Entry> {

        @Override
        protected final void setNext(Entry node, Entry next) {
            node.next = next;
        }

        @Override
        protected final Entry next(Entry node) {
            return node.next;
        }

        @Override
        protected final int hashKey(Entry node) {
            return hashKey(node.id);
        }

        @Override
        protected final boolean equalKeys(Entry lhs, Entry rhs) {
            return lhs.id == rhs.id;
        }

        @Override
        protected final boolean equalKeys(Entry lhs, long rhs) {
            return lhs.id == rhs;
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

        final Entry marayl = new Entry(1, "Mark");
        final Entry gosayl = new Entry(2, "Goska");
        final Entry tobayl = new Entry(3, "Toby");
        final Entry emiayl = new Entry(4, "Emily");

        final EntryHashTable t = new EntryHashTable();
        assertNull(t.insert(marayl));
        assertFalse(t.isEmpty());
        assertEquals(1, t.size());
        assertNull(t.insert(gosayl));
        assertFalse(t.isEmpty());
        assertEquals(2, t.size());
        assertNull(t.insert(tobayl));
        assertFalse(t.isEmpty());
        assertEquals(3, t.size());
        assertNull(t.insert(emiayl));
        assertFalse(t.isEmpty());
        assertEquals(4, t.size());
    }

    @Test
    public final void testReplace() {

        final Entry marayl = new Entry(1, "Mark");
        final Entry gosayl = new Entry(2, "Goska");
        final Entry tobayl = new Entry(3, "Toby");
        final Entry emiayl = new Entry(4, "Emily");

        final EntryHashTable t = new EntryHashTable();
        t.insert(marayl);
        t.insert(gosayl);
        assertSame(marayl, t.insert(new Entry(1, "Mark")));
        assertEquals(2, t.size());
        assertSame(gosayl, t.insert(new Entry(2, "Goska")));
        assertEquals(2, t.size());

        t.insert(tobayl);
        t.insert(emiayl);
        assertSame(tobayl, t.insert(new Entry(3, "Toby")));
        assertEquals(4, t.size());
        assertSame(emiayl, t.insert(new Entry(4, "Emily")));
        assertEquals(4, t.size());
    }

    @Test
    public final void testRemove() {

        final Entry marayl = new Entry(1, "Mark");
        final Entry gosayl = new Entry(2, "Goska");
        final Entry tobayl = new Entry(3, "Toby");
        final Entry emiayl = new Entry(4, "Emily");

        final EntryHashTable t = new EntryHashTable();
        t.insert(marayl);
        t.insert(gosayl);
        t.insert(tobayl);
        t.insert(emiayl);

        assertSame(marayl, t.remove(1));
        assertEquals(3, t.size());
        assertSame(gosayl, t.remove(2));
        assertEquals(2, t.size());
        assertSame(tobayl, t.remove(3));
        assertEquals(1, t.size());
        assertSame(emiayl, t.remove(4));
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
        assertNull(t.remove(5));
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
    }

    @Test
    public final void testFind() {

        final Entry marayl = new Entry(1, "Mark");
        final Entry gosayl = new Entry(2, "Goska");
        final Entry tobayl = new Entry(3, "Toby");
        final Entry emiayl = new Entry(4, "Emily");

        final EntryHashTable t = new EntryHashTable();
        t.insert(marayl);
        t.insert(gosayl);
        t.insert(tobayl);
        t.insert(emiayl);

        assertSame(marayl, t.find(1));
        assertSame(gosayl, t.find(2));
        assertSame(tobayl, t.find(3));
        assertSame(emiayl, t.find(4));
        assertNull(t.find(5));
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.junit.Test;

import static org.junit.Assert.*;

import com.swirlycloud.twirly.node.BasicSlNode;

public final class HashTableTest {

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

    private static final class EntryHashTable extends HashTable<Entry> {

        private static int hashCode(long id) {
            return (int) (id ^ id >>> 32);
        }

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
            return hashCode(node.id);
        }

        @Override
        protected final boolean equalKeys(Entry lhs, Entry rhs) {
            return lhs.id == rhs.id;
        }

        public EntryHashTable() {
            super(12);
        }

        @SuppressWarnings("unused")
        public final Entry remove(long id) {
            final int bucket = indexFor(hashCode(id), this.buckets.length);
            Entry it = buckets[bucket];
            if (it == null) {
                return null;
            }
            if (it.id == id) {
                buckets[bucket] = it.next;
                return it;
            }
            for (; it.next != null; it = it.next) {
                final Entry next = it.next;
                if (next.id == id) {
                    it.next = next.next;
                    return next;
                }
            }
            return null;
        }

        public final Entry find(long id) {
            final int bucket = indexFor(hashCode(id), buckets.length);
            for (Entry it = buckets[bucket]; it != null; it = it.next) {
                if (it.id == id) {
                    return it;
                }
            }
            return null;
        }
    }

    @Test
    public final void testInsert() {
        final EntryHashTable t = new EntryHashTable();
        final Entry marayl = new Entry(1, "Mark");
        final Entry gosayl = new Entry(2, "Goska");
        final Entry tobayl = new Entry(3, "Toby");
        final Entry emiayl = new Entry(4, "Emily");
        t.insert(marayl);
        t.insert(gosayl);
        t.insert(tobayl);
        t.insert(emiayl);
        //assertSame(marayl, t.find(1));
    }
}

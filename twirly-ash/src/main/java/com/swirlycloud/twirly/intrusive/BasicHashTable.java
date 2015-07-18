/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

/**
 * Hashtable with a single key derived from Object.
 * 
 * @param <K>
 *            The key type.
 * @param <V>
 *            The element or value type.
 * 
 * @author Mark Aylett
 */
public abstract class BasicHashTable<K, V> extends HashTable<V> {

    protected abstract boolean equalKeyDirect(V lhs, K rhs);

    public BasicHashTable(int capacity) {
        super(capacity);
    }

    public final V remove(K key) {
        if (isEmpty()) {
            return null;
        }
        int i = indexFor(key.hashCode(), buckets.length);
        V it = getBucket(i);
        if (it == null) {
            return null;
        }
        // Check if the first element in the bucket has an equivalent key.
        if (equalKeyDirect(it, key)) {
            buckets[i] = next(it);
            --size;
            return it;
        }
        // Check if a subsequent element in the bucket has an equivalent key.
        for (V next; (next = next(it)) != null; it = next) {
            if (equalKeyDirect(next, key)) {
                setNext(it, next(next));
                --size;
                return next;
            }
        }
        return null;
    }

    public final V find(K key) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(key.hashCode(), buckets.length);
        for (V it = getBucket(i); it != null; it = next(it)) {
            if (equalKeyDirect(it, key)) {
                return it;
            }
        }
        return null;
    }
}

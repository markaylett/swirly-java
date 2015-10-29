/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.CollectionUtil.hashLong;

/**
 * Hashtable with key of type long.
 * 
 * @param <V>
 *            The concrete element type.
 * 
 * @author Mark Aylett
 */
public abstract class LongMap<V> extends Map<V> {

    protected abstract boolean equalKey(V lhs, long rhs);

    public LongMap(int capacity) {
        super(capacity);
    }

    public final V remove(long key) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashLong(key), buckets.length);
        V it = getBucket(i);
        if (it == null) {
            return null;
        }
        // Check if the first element in the bucket has an equivalent key.
        if (equalKey(it, key)) {
            buckets[i] = next(it);
            --size;
            return it;
        }
        // Check if a subsequent element in the bucket has an equivalent key.
        for (V next; (next = next(it)) != null; it = next) {
            if (equalKey(next, key)) {
                setNext(it, next(next));
                --size;
                return next;
            }
        }
        return null;
    }

    public final V find(long key) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashLong(key), buckets.length);
        for (V it = getBucket(i); it != null; it = next(it)) {
            if (equalKey(it, key)) {
                return it;
            }
        }
        return null;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

/**
 * Hashtable with key of type String.
 * 
 * @param <T>
 *            The concrete element type.
 */
public abstract class StringHashTable<T> extends HashTable<T> {

    protected abstract boolean equalKeys(T lhs, String rhs);

    public StringHashTable(int capacity) {
        super(capacity);
    }

    public final T remove(String key) {
        if (isEmpty()) {
            return null;
        }
        int i = indexFor(key.hashCode(), buckets.length);
        T it = getBucket(i);
        if (it == null) {
            return null;
        }
        // Check if the first element in the bucket has an equivalent key.
        if (equalKeys(it, key)) {
            buckets[i] = next(it);
            --size;
            return it;
        }
        // Check if a subsequent element in the bucket has an equivalent key.
        for (T next; (next = next(it)) != null; it = next) {
            if (equalKeys(next, key)) {
                setNext(it, next(next));
                --size;
                return next;
            }
        }
        return null;
    }

    public final T find(String key) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(key.hashCode(), buckets.length);
        for (T it = getBucket(i); it != null; it = next(it)) {
            if (equalKeys(it, key)) {
                return it;
            }
        }
        return null;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

/**
 * Hashtable with key of type long.
 * 
 * @param <T>
 *            The concrete element type.
 */
public abstract class HashTableLong<T> extends HashTable<T> {

    protected static int hashKey(long id) {
        return (int) (id ^ id >>> 32);
    }

    protected abstract boolean equalKeys(T lhs, long rhs);

    public HashTableLong(int capacity) {
        super(capacity);
    }

    public final T remove(long key) {
        if (isEmpty()) {
            return null;
        }
        int i = indexFor(hashKey(key), buckets.length);
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

    public final T find(long key) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashKey(key), buckets.length);
        for (T it = getBucket(i); it != null; it = next(it)) {
            if (equalKeys(it, key)) {
                return it;
            }
        }
        return null;
    }
}

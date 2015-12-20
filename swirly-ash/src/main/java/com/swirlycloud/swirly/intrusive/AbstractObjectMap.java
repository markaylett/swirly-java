/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.intrusive;

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
public abstract class AbstractObjectMap<K, V> extends AbstractMap<V> {

    protected abstract boolean equalKey(V lhs, K rhs);

    public AbstractObjectMap(int capacity) {
        super(capacity);
    }

    public final V remove(K key) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(key.hashCode(), buckets.length);
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

    public final V find(K key) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(key.hashCode(), buckets.length);
        for (V it = getBucket(i); it != null; it = next(it)) {
            if (equalKey(it, key)) {
                return it;
            }
        }
        return null;
    }
}

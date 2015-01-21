/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.math.MathUtil.nextPow2;

import java.io.PrintStream;

public abstract class HashTable<T> {
    private static final Object[] EMPTY = {};
    private static final int MIN_BUCKETS = 1 << 4;

    protected T[] buckets;
    private int size;
    private int threshold;

    protected abstract void setNext(T node, T next);

    protected abstract T next(T node);

    protected abstract int hashKey(T node);

    protected abstract boolean equalKeys(T lhs, T rhs);

    protected static int indexFor(int h, int length) {
        // Doug Lea's supplemental secondaryHash function.
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7) ^ (h >>> 4);
        // Assumes that length is a power of two.
        return h & (length - 1);
    }

    /**
     * The threshold at which the hash-table will grow.
     * 
     * @param capacity
     *            The number of buckets.
     * @return the threshold.
     */
    private static int thresholdFor(int capacity) {
        // Threshold is 2/3 capacity.
        return (capacity << 1) / 3;
    }

    private final void grow(int capacity) {
        @SuppressWarnings("unchecked")
        final T[] newBuckets = (T[]) new Object[capacity];
        for (int i = 0; i < buckets.length; ++i) {
            while (buckets[i] != null) {
                // Pop.
                final T node = buckets[i];
                buckets[i] = next(node);
                // Push.
                final int j = indexFor(hashKey(node), newBuckets.length);
                setNext(node, newBuckets[j]);
                newBuckets[j] = node;
            }
        }
        buckets = newBuckets;
        threshold = thresholdFor(capacity);
    }

    @SuppressWarnings("unchecked")
    public HashTable(int capacity) {
        // Postpone allocation until first insert.
        this.buckets = (T[]) EMPTY;
        this.size = 0;
        // N.B. the threshold is used to store the desired capacity on construction. It is not used
        // for its intended purpose until grow() is called.
        if (capacity < MIN_BUCKETS) {
            threshold = MIN_BUCKETS;
        } else {
            threshold = nextPow2(capacity);
        }
    }

    public final void print(PrintStream s) {
        for (int i = 0; i < buckets.length; ++i) {
            s.print('|');
            for (T it = buckets[i]; it != null; it = next(it)) {
                s.print('*');
            }
            s.println();
        }
    }

    public final void insert(T node) {
        if (buckets.length == 0) {
            // First item.
            grow(threshold);
        } else if (size >= threshold) {
            grow(buckets.length << 1);
        }
        final int i = indexFor(hashKey(node), buckets.length);
        setNext(node, buckets[i]);
        buckets[i] = node;
        ++size;
    }

    public final boolean contains(T node) {
        final int i = indexFor(hashKey(node), buckets.length);
        for (T it = buckets[i]; it != null; it = next(it)) {
            if (equalKeys(node, it)) {
                return true;
            }
        }
        return false;
    }
}

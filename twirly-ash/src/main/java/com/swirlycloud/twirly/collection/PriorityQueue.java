/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;

public final class PriorityQueue<V> implements Sequence<V> {

    private final Comparator<? super V> comp;
    private Object[] elems;
    private int size;

    private static int leftChild(int i) {
        // i * 2
        return i << 1;
    }

    private static int parent(int i) {
        // i / 2
        return i >> 1;
    }

    private final void grow() {
        // Current capacity for one-based index.
        int capacity = elems.length - 1;
        // Grow capacity by 50%.
        capacity = (capacity * 3) / 2;
        elems = Arrays.copyOf(elems, 1 + capacity);
    }

    private final void swap(int i, int j) {
        final Object tmp = elems[i];
        elems[i] = elems[j];
        elems[j] = tmp;
    }

    /**
     * Heap invariant: If c is a child node of p, then p <= c. I.e. any node is greater than or
     * equal to its parent.
     */

    @SuppressWarnings("unchecked")
    private final boolean invariant(int c, int p) {
        return comp.compare((V) elems[p], (V) elems[c]) <= 0;
    }

    private final void siftUp(int n) {
        // heap(1, n - 1)
        int c, p;
        // While child is not root.
        for (c = n; c != 1; c = p) {
            p = parent(c);
            if (invariant(c, p)) {
                break;
            }
            // Restore invariant.
            swap(c, p);
        }
        // heap(1, n)
    }

    @SuppressWarnings("unchecked")
    private final void siftDown(int n) {
        // heap(2, n)
        int c, p;
        // While parent has child.
        for (p = 1; (c = leftChild(p)) != 0 && c <= n; p = c) {
            // Use child with lower value.
            final int r = c + 1;
            if (r <= n && comp.compare((V) elems[r], (V) elems[c]) < 0) {
                c = r;
            }
            if (invariant(c, p)) {
                break;
            }
            // Restore invariant.
            swap(c, p);
        }
        // heap(1, n)
    }

    public PriorityQueue(Comparator<? super V> comp) {
        this.comp = comp;
        this.elems = new Object[64];
        this.size = 0;
    }

    public PriorityQueue() {
        this(new Comparator<V>() {
            @SuppressWarnings("unchecked")
            @Override
            public final int compare(V lhs, V rhs) {
                final Comparable<? super V> comp = (Comparable<? super V>) lhs;
                return comp.compareTo(rhs);
            }
        });
    }

    @Override
    public final void clear() {
        // One-based index.
        for (int i = 1; i <= size; ++i) {
            elems[i] = null;
        }
        size = 0;
    }

    @Override
    public final void add(@NonNull V elem) {
        if (elems.length <= size) {
            grow();
        }
        // Push back.
        elems[++size] = elem;
        // Restore invariant.
        // heap(1, n - 1)
        siftUp(size);
        // heap(1, n)
    }

    public final V removeFirst() {
        // Root has lowest value.
        final V elem = getFirst();
        // Fill gap with last.
        elems[1] = elems[size--];
        // Restore invariant.
        // heap(2, n)
        siftDown(size);
        // heap(1, n)
        return elem;
    }

    public final V pop() {
        return removeFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final V getFirst() {
        if (size < 1) {
            throw new NoSuchElementException();
        }
        // Root has lowest value.
        return (V) elems[1];
    }

    @Override
    public final boolean isEmpty() {
        return size == 0;
    }
}

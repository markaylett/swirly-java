/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.collection.Sequence;

public abstract class AbstractStack<V> implements Sequence<V> {
    private V first;

    protected abstract void setNext(V node, V next);

    protected abstract V next(V node);

    @Override
    public final void clear() {
        first = null;
    }

    @Override
    public final void add(@NonNull V node) {
        insertFront(node);
    }

    public final void insertFront(V node) {
        setNext(node, first);
        first = node;
    }

    public final V removeFirst() {
        final V node = first;
        first = next(first);
        return node;
    }

    public final V pop() {
        return removeFirst();
    }

    public final void push(V node) {
        insertFront(node);
    }

    @Override
    public final V getFirst() {
        return first;
    }

    @Override
    public final boolean isEmpty() {
        return first == null;
    }
}

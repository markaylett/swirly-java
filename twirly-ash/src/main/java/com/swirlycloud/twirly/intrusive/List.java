/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

public abstract @NonNullByDefault class List<V> implements Container<V> {
    private final V end;

    protected abstract void insert(V node, V prev, V next);

    protected abstract void insertBefore(V node, V next);

    protected abstract void insertAfter(V node, V prev);

    protected abstract void remove(V node);

    protected abstract void setPrev(V node, V prev);

    protected abstract void setNext(V node, V next);

    protected abstract V next(V node);

    protected abstract V prev(V node);

    protected List(V end) {
        this.end = end;
        clear();
    }

    @Override
    public final void clear() {
        setPrev(end, end);
        setNext(end, end);
    }

    @Override
    public final void add(@NonNull V node) {
        insertBack(node);
    }

    public final void insertFront(V node) {
        insertBefore(node, next(end));
    }

    public final void insertBack(V node) {
        insertAfter(node, prev(end));
    }

    public final V removeFirst() {
        assert !isEmpty();
        final V node = next(end);
        remove(node);
        return node;
    }

    public final V removeLast() {
        assert !isEmpty();
        final V node = prev(end);
        remove(node);
        return node;
    }

    public final V pop() {
        return removeLast();
    }

    public final void push(V node) {
        insertFront(node);
    }

    @Override
    public final V getFirst() {
        return next(end);
    }

    public final V getLast() {
        return prev(end);
    }

    @Override
    public final boolean isEmpty() {
        return next(end) == end;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class List<T> {
    private final T end;

    protected abstract void insert(T node, T prev, T next);

    protected abstract void insertBefore(T node, T next);

    protected abstract void insertAfter(T node, T prev);

    protected abstract void remove(T node);

    protected abstract void setPrev(T node, T prev);

    protected abstract void setNext(T node, T next);

    protected abstract T next(T node);

    protected abstract T prev(T node);

    public List(T end) {
        this.end = end;
        clear();
    }

    public final void clear() {
        setPrev(end, end);
        setNext(end, end);
    }

    public final void insertFront(T node) {
        insertBefore(node, next(end));
    }

    public final void insertBack(T node) {
        insertAfter(node, prev(end));
    }

    public final T removeFirst() {
        assert !isEmpty();
        final T node = next(end);
        remove(node);
        return node;
    }

    public final T removeLast() {
        assert !isEmpty();
        final T node = prev(end);
        remove(node);
        return node;
    }

    public final T pop() {
        return removeLast();
    }

    public final void push(T node) {
        insertFront(node);
    }

    public final T getFirst() {
        return next(end);
    }

    public final T getLast() {
        return prev(end);
    }

    public final boolean isEmpty() {
        return next(end) == end;
    }
}

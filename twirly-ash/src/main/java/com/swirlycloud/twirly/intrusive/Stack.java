/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class Stack<T> {
    private T first;

    protected abstract void setNext(T node, T next);

    protected abstract T next(T node);

    public final void clear() {
        first = null;
    }

    public final void insertFront(T node) {
        setNext(node, first);
        first = node;
    }

    public final T removeFirst() {
        final T node = first;
        first = next(first);
        return node;
    }

    public final T pop() {
        return removeFirst();
    }

    public final void push(T node) {
        insertFront(node);
    }

    public final T getFirst() {
        return first;
    }

    public final boolean isEmpty() {
        return first == null;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class Stack<V> {
    private V first;

    protected abstract void setNext(V node, V next);

    protected abstract V next(V node);

    public final void clear() {
        first = null;
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

    public final V getFirst() {
        return first;
    }

    public final boolean isEmpty() {
        return first == null;
    }
}

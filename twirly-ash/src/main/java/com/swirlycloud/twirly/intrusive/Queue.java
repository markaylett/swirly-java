/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class Queue<T> {
    private T first;
    private T last;

    protected abstract void setNext(T node, T next);

    protected abstract T next(T node);

    public final void clear() {
        first = null;
        last = null;
    }

    public final void insertBack(T node) {
        if (!isEmpty()) {
            setNext(last, node);
        } else {
            first = node;
        }
        last = node;
        setNext(node, null);
    }

    public final T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        final T node = first;
        first = next(first);
        if (isEmpty()) {
            last = null;
        }
        return node;
    }

    public final void join(Queue<T> rhs) {
        if (!rhs.isEmpty()) {
            if (!isEmpty()) {
                setNext(last, rhs.first);
            } else {
                first = rhs.first;
            }
            last = rhs.last;
        }
    }

    public final T getFirst() {
        return first;
    }

    public final T getLast() {
        return last;
    }

    public final boolean isEmpty() {
        return first == null;
    }
}

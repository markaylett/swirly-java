/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

@Deprecated
public final class Queue {
    private SlNode first;
    private SlNode last;

    public final void clear() {
        first = null;
        last = null;
    }

    public final void insertBack(SlNode node) {
        if (!isEmpty()) {
            last.setSlNext(node);
        } else {
            first = node;
        }
        last = node;
        node.setSlNext(null);
    }

    public final SlNode removeFirst() {
        if (isEmpty()) {
            return null;
        }
        final SlNode node = first;
        first = first.slNext();
        if (isEmpty()) {
            last = null;
        }
        return node;
    }

    public final void join(Queue rhs) {
        if (!rhs.isEmpty()) {
            if (!isEmpty()) {
                last.setSlNext(rhs.first);
            } else {
                first = rhs.first;
            }
            last = rhs.last;
        }
    }

    public final SlNode getFirst() {
        return first;
    }

    public final SlNode getLast() {
        return last;
    }

    public final boolean isEmpty() {
        return first == null;
    }
}

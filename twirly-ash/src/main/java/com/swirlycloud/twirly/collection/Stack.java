/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

public final class Stack {
    private SlNode first;

    public final void clear() {
        first = null;
    }

    public final void insertFront(SlNode node) {
        node.setSlNext(first);
        first = node;
    }

    public final SlNode removeFirst() {
        final SlNode node = first;
        first = node.slNext();
        return node;
    }

    public final SlNode pop() {
        return removeFirst();
    }

    public final void push(SlNode node) {
        insertFront(node);
    }

    public final SlNode getFirst() {
        return first;
    }

    public final boolean isEmpty() {
        return first == null;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

public final class Stack {
    private SlNode first;

    public final void clear() {
        first = null;
    }

    public final void insertAfter(SlNode node, SlNode newNode) {
        newNode.setSlNext(node.slNext());
        node.setSlNext(newNode);
    }

    public final void insertFront(SlNode newNode) {
        newNode.setSlNext(first);
        first = newNode;
    }

    public final SlNode removeFirst() {
        final SlNode node = first;
        first = node.slNext();
        return node;
    }

    public final SlNode pop() {
        return removeFirst();
    }

    public final void push(SlNode newNode) {
        insertFront(newNode);
    }

    public final SlNode getFirst() {
        return first;
    }

    public final boolean isEmpty() {
        return first == null;
    }
}

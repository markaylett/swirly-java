/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

public final class Stack {
    private SlNode first;

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

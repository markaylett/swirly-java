/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

public final class Queue {
    private SlNode first;
    private SlNode last;

    public final void insertBack(SlNode node) {
        if (!isEmpty()) {
            last.setNext(node);
        } else {
            first = node;
        }
        last = node;
        node.setNext(null);
    }

    public final SlNode removeFirst() {
        if (isEmpty()) {
            return null;
        }
        final SlNode node = first;
        first = first.slNext();
        if (isEmpty())
            last = null;
        return node;
    }

    public final void join(Queue rhs) {
        if (!rhs.isEmpty()) {
            if (!isEmpty())
                last.setNext(rhs.first);
            else
                first = rhs.first;
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

/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

public final class List {
    private final DlNode end = new BasicDlNode() {
        @Override
        public final boolean isEnd() {
            return true;
        }
    };

    public List() {
        clear();
    }

    public final void clear() {
        end.setDlPrev(end);
        end.setDlNext(end);
    }

    public final void insertFront(BasicDlNode newNode) {
        newNode.insertBefore(end.dlNext());
    }

    public final void insertBack(DlNode newNode) {
        newNode.insertAfter(end.dlPrev());
    }

    public final DlNode removeFirst() {
        assert !isEmpty();
        final DlNode node = this.end.dlNext();
        node.remove();
        return node;
    }

    public final DlNode removeLast() {
        assert !isEmpty();
        final DlNode node = this.end.dlPrev();
        node.remove();
        return node;
    }

    public final DlNode pop() {
        return removeLast();
    }

    public final void push(BasicDlNode newNode) {
        insertFront(newNode);
    }

    public final DlNode getFirst() {
        return this.end.dlNext();
    }

    public final DlNode getLast() {
        return this.end.dlPrev();
    }

    public final boolean isEmpty() {
        return this.end.dlNext() == this.end;
    }
}

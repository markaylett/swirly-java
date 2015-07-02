/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.node.BasicDlNode;
import com.swirlycloud.twirly.node.DlNode;

public final class DlList extends List<DlNode> {

    @Override
    protected final void insert(DlNode node, DlNode prev, DlNode next) {
        node.insert(prev, next);
    }

    @Override
    protected final void insertBefore(DlNode node, DlNode next) {
        node.insertBefore(next);
    }

    @Override
    protected final void insertAfter(DlNode node, DlNode prev) {
        node.insertAfter(prev);
    }

    @Override
    protected final void remove(DlNode node) {
        node.remove();
    }

    @Override
    protected final void setPrev(DlNode node, @NonNull DlNode prev) {
        node.setDlPrev(prev);
    }

    @Override
    protected final void setNext(DlNode node, @NonNull DlNode next) {
        node.setDlNext(next);
    }

    @Override
    protected final DlNode next(DlNode node) {
        return node.dlNext();
    }

    @Override
    protected final DlNode prev(DlNode node) {
        return node.dlPrev();
    }

    public DlList() {
        super(new BasicDlNode() {
            @Override
            public final boolean isEnd() {
                return true;
            }
        });
    }
}

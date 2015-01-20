/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import com.swirlycloud.twirly.collection.RbNode;

public final class RbTree extends Tree<RbNode> {

    @Override
    protected final RbNode setLeft(RbNode node, RbNode left) {
        return node.setLeft(left);
    }

    @Override
    protected final RbNode setRight(RbNode node, RbNode right) {
        return node.setRight(right);
    }

    @Override
    protected final RbNode setParent(RbNode node, RbNode parent) {
        return node.setParent(parent);
    }

    @Override
    protected final void setColor(RbNode node, int color) {
        node.setColor(color);
    }

    @Override
    protected final RbNode next(RbNode node) {
        return node.rbNext();
    }

    @Override
    protected final RbNode prev(RbNode node) {
        return node.rbPrev();
    }

    @Override
    protected final long getKey(RbNode node) {
        return node.getKey();
    }

    @Override
    protected final RbNode getLeft(RbNode node) {
        return node.getLeft();
    }

    @Override
    protected final RbNode getRight(RbNode node) {
        return node.getRight();
    }

    @Override
    protected final RbNode getParent(RbNode node) {
        return node.getParent();
    }

    @Override
    protected final int getColor(RbNode node) {
        return node.getColor();
    }
}

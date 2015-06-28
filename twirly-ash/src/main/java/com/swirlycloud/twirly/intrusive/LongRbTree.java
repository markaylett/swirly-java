/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import com.swirlycloud.twirly.node.RbNode;

public abstract class LongRbTree extends LongTree<RbNode> {

    @Override
    protected final void setNode(RbNode lhs, RbNode rhs) {
        lhs.setNode(rhs);
    }

    @Override
    protected final void setLeft(RbNode node, RbNode left) {
        node.setLeft(left);
    }

    @Override
    protected final void setRight(RbNode node, RbNode right) {
        node.setRight(right);
    }

    @Override
    protected final void setParent(RbNode node, RbNode parent) {
        node.setParent(parent);
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

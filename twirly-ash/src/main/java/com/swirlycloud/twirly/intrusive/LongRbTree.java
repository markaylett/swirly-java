/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.RbNode;

public abstract @NonNullByDefault class LongRbTree extends LongTree<RbNode> {

    @Override
    protected final void setNode(RbNode lhs, RbNode rhs) {
        lhs.setNode(rhs);
    }

    @Override
    protected final void setLeft(RbNode node, @Nullable RbNode left) {
        node.setLeft(left);
    }

    @Override
    protected final void setRight(RbNode node, @Nullable RbNode right) {
        node.setRight(right);
    }

    @Override
    protected final void setParent(RbNode node, @Nullable RbNode parent) {
        node.setParent(parent);
    }

    @Override
    protected final void setColor(RbNode node, int color) {
        node.setColor(color);
    }

    @Override
    protected final @Nullable RbNode next(RbNode node) {
        return node.rbNext();
    }

    @Override
    protected final @Nullable RbNode prev(RbNode node) {
        return node.rbPrev();
    }

    @Override
    protected final @Nullable RbNode getLeft(RbNode node) {
        return node.getLeft();
    }

    @Override
    protected final @Nullable RbNode getRight(RbNode node) {
        return node.getRight();
    }

    @Override
    protected final @Nullable RbNode getParent(RbNode node) {
        return node.getParent();
    }

    @Override
    protected final int getColor(RbNode node) {
        return node.getColor();
    }
}

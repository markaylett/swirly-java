/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import static com.swirlycloud.twirly.util.CollectionUtil.compareLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.intrusive.AbstractLongTree;

public final @NonNullByDefault class LevelTree extends AbstractLongTree<Level> {

    private static final long serialVersionUID = 1L;

    @Override
    protected final void setNode(Level lhs, Level rhs) {
        lhs.setNode(rhs);
    }

    @Override
    protected final void setLeft(Level node, @Nullable Level left) {
        node.setLeft(left);
    }

    @Override
    protected final void setRight(Level node, @Nullable Level right) {
        node.setRight(right);
    }

    @Override
    protected final void setParent(Level node, @Nullable Level parent) {
        node.setParent(parent);
    }

    @Override
    protected final void setColor(Level node, int color) {
        node.setColor(color);
    }

    @Override
    protected final @Nullable Level next(Level node) {
        return (Level) node.rbNext();
    }

    @Override
    protected final @Nullable Level prev(Level node) {
        return (Level) node.rbPrev();
    }

    @Override
    protected final @Nullable Level getLeft(Level node) {
        return (Level) node.getLeft();
    }

    @Override
    protected final @Nullable Level getRight(Level node) {
        return (Level) node.getRight();
    }

    @Override
    protected final @Nullable Level getParent(Level node) {
        return (Level) node.getParent();
    }

    @Override
    protected final int getColor(Level node) {
        return node.getColor();
    }

    @Override
    protected final int compareNode(Level lhs, Level rhs) {
        return compareLong(lhs.getKey(), rhs.getKey());
    }

    @Override
    protected final int compareKey(Level lhs, long rhs) {
        return compareLong(lhs.getKey(), rhs);
    }
}

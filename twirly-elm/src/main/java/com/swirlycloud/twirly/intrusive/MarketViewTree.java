/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.MarketView;

public final @NonNullByDefault class MarketViewTree extends ObjectTree<String, MarketView> {

    private static final long serialVersionUID = 1L;

    @Override
    protected final void setNode(MarketView lhs, MarketView rhs) {
        lhs.setNode(rhs);
    }

    @Override
    protected final void setLeft(MarketView node, @Nullable MarketView left) {
        node.setLeft(left);
    }

    @Override
    protected final void setRight(MarketView node, @Nullable MarketView right) {
        node.setRight(right);
    }

    @Override
    protected final void setParent(MarketView node, @Nullable MarketView parent) {
        node.setParent(parent);
    }

    @Override
    protected final void setColor(MarketView node, int color) {
        node.setColor(color);
    }

    @Override
    protected final @Nullable MarketView next(MarketView node) {
        return (MarketView) node.rbNext();
    }

    @Override
    protected final @Nullable MarketView prev(MarketView node) {
        return (MarketView) node.rbPrev();
    }

    @Override
    protected final @Nullable MarketView getLeft(MarketView node) {
        return (MarketView) node.getLeft();
    }

    @Override
    protected final @Nullable MarketView getRight(MarketView node) {
        return (MarketView) node.getRight();
    }

    @Override
    protected final @Nullable MarketView getParent(MarketView node) {
        return (MarketView) node.getParent();
    }

    @Override
    protected final int getColor(MarketView node) {
        return node.getColor();
    }

    @Override
    protected final int compareNode(MarketView lhs, MarketView rhs) {
        return lhs.getMnem().compareTo(rhs.getMnem());
    }

    @Override
    protected final int compareKey(MarketView lhs, String rhs) {
        return lhs.getMnem().compareTo(rhs);
    }
}

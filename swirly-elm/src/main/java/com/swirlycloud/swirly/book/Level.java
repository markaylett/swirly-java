/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.book;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.entity.Order;
import com.swirlycloud.swirly.node.AbstractRbNode;
import com.swirlycloud.swirly.node.DlNode;

/**
 * Price level.
 * 
 * A price level is an aggregation of orders by price. I.e. the sum of all orders in the book at the
 * same price.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class Level extends AbstractRbNode {

    private static final long serialVersionUID = 1L;

    transient Order firstOrder;
    private final long key;
    private final long ticks;
    /**
     * Must be greater than zero.
     */
    private long resd;
    /**
     * Must be greater than zero.
     */
    int count;

    Level(Order firstOrder) {
        final long ticks = firstOrder.getTicks();
        this.firstOrder = firstOrder;
        this.key = composeKey(firstOrder.getSide(), ticks);
        this.ticks = ticks;
        this.resd = firstOrder.getResd();
        this.count = 1;
    }

    /**
     * Synthetic level key.
     */
    static long composeKey(Side side, long ticks) {
        return side == Side.BUY ? -ticks : ticks;
    }

    final void reduce(long delta) {
        this.resd -= delta;
    }

    final void addOrder(Order order) {
        resd += order.getResd();
        ++count;
    }

    final void subOrder(Order order) {
        resd -= order.getResd();
        --count;
    }

    final DlNode getFirstOrder() {
        return firstOrder;
    }

    final long getKey() {
        return key;
    }

    public final long getTicks() {
        return ticks;
    }

    public final long getResd() {
        return resd;
    }

    public final int getCount() {
        return count;
    }
}

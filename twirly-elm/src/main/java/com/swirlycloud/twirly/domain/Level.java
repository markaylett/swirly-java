/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.DlNode;

/**
 * Price level.
 * 
 * A price level is an aggregation of orders by price. I.e. the sum of all orders in the book at the
 * same price.
 */
public final @NonNullByDefault class Level extends BasicRbNode {
    final long key;
    final long ticks;
    Order firstOrder;
    /**
     * Must be greater than zero.
     */
    long lots;
    /**
     * Must be greater than zero.
     */
    int count;

    public Level(Order order) {
        final long ticks = order.getTicks();
        this.key = composeKey(order.getAction(), ticks);
        this.ticks = ticks;
        this.firstOrder = order;
        this.lots = order.getResd();
        this.count = 1;
    }

    /**
     * Synthetic level key.
     */

    public static long composeKey(Action action, long ticks) {
        return action == Action.BUY ? -ticks : ticks;
    }

    public final long getKey() {
        return key;
    }

    public final void addOrder(Order order) {
        lots += order.getResd();
        ++count;
    }

    public final long getTicks() {
        return ticks;
    }

    public final DlNode getFirstOrder() {
        return firstOrder;
    }

    public final long getLots() {
        return lots;
    }

    public final int getCount() {
        return count;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.DlNode;

/**
 * A level is an aggregation of orders by price.
 */
public final class Level extends BasicRbNode {
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

    @Override
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

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import org.doobry.util.BasicRbNode;

/**
 * A level is an aggregation of orders by price.
 */
public final class Level extends BasicRbNode {
    Order firstOrder;
    final long key;
    final long ticks;
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
        this.firstOrder = order;
        this.key = toKey(order.getAction(), ticks);
        this.ticks = ticks;
        this.lots = order.getResd();
        this.count = 1;
    }

    /**
     * Synthetic position key.
     */

    public static long toKey(Action action, long ticks) {
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

    public final Order getFirstOrder() {
        return firstOrder;
    }

    public final long getTicks() {
        return ticks;
    }

    public final long getLots() {
        return lots;
    }

    public final int getCount() {
        return count;
    }
}

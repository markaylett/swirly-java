/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import org.doobry.util.BasicRbNode;
import org.doobry.util.DlNode;

/**
 * A level is an aggregation of orders by price.
 */
public final class Level extends BasicRbNode {
    Order firstOrder;
    final long id;
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
        this.id = toId(order.getAction(), ticks);
        this.ticks = ticks;
        this.lots = order.getResd();
        this.count = 1;
    }

    /**
     * Synthetic position id.
     */

    public static long toId(Action action, long ticks) {
        return action == Action.BUY ? -ticks : ticks;
    }

    @Override
    public final long getId() {
        return id;
    }

    public final void addOrder(Order order) {
        lots += order.getResd();
        ++count;
    }

    public final DlNode getFirstOrder() {
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

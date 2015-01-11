/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.collection.DlNode;
import com.swirlycloud.twirly.collection.List;
import com.swirlycloud.twirly.collection.RbNode;
import com.swirlycloud.twirly.collection.Tree;

public final class Side {
    private final Tree levels = new Tree();
    private final List orders = new List();

    // Last trade.
    private long lastTicks = 0;
    private long lastLots = 0;
    private long lastTime = 0;

    private final Level getLazyLevel(Order order) {
        final long key = Level.composeKey(order.getAction(), order.getTicks());
        final RbNode node = levels.pfind(key);

        Level level;
        if (node == null || node.getKey() != key) {
            level = new Level(order);
            levels.pinsert(level, node);
        } else {
            level = (Level) node;
            level.addOrder(order);
        }
        order.level = level;
        return level;
    }

    private final void reduce(Order order, long delta) {
        assert order != null;
        assert order.level != null;
        assert delta >= 0 && delta <= order.getResd();

        if (delta < order.getResd()) {
            // Reduce level and order by lots.
            final Level level = (Level) order.level;
            level.lots -= delta;
            order.resd -= delta;
        } else {
            assert delta == order.getResd();
            removeOrder(order);
            order.resd = 0;
        }
    }

    /**
     * Insert order into side. Assumes that the order does not already belong to a side. I.e. it
     * assumes that level member is null. Assumes that order-id and reference are unique.
     */
    public final void insertOrder(Order order) {

        assert order != null;
        assert order.level == null;
        assert order.getTicks() != 0;
        assert order.resd > 0;
        assert order.exec <= order.lots;
        assert order.lots > 0;
        assert order.getMinLots() >= 0;

        final Level level = getLazyLevel(order);
        final Level nextLevel = (Level) level.rbNext();

        if (nextLevel != null) {
            // Insert order after the level's last order.
            // I.e. insert order before the next level's first order.
            order.insertBefore(nextLevel.firstOrder);
        } else {
            orders.insertBack(order);
        }
    }

    /**
     * Internal housekeeping aside, the state of the order is not affected by this function.
     */
    public final void removeOrder(Order order) {
        assert order != null;
        assert order.level != null;

        final Level level = (Level) order.level;
        level.lots -= order.resd;

        if (--level.count == 0) {
            // Remove level.
            assert level.lots == 0;
            levels.remove(level);
        } else if (level.firstOrder == order) {
            // First order at this level is being removed.
            level.firstOrder = (Order) order.dlNext();
        }

        order.remove();

        // No longer associated with side.
        order.level = null;
    }

    public final void placeOrder(Order order, long now) {
        order.place(now);
        insertOrder(order);
    }

    public final void reviseOrder(Order order, long lots, long now) {
        assert order != null;
        assert order.level != null;
        assert lots > 0;
        assert lots >= order.exec && lots >= order.getMinLots() && lots <= order.lots;

        final long delta = order.lots - lots;

        // This will increase order revision.
        reduce(order, delta);

        order.state = State.REVISE;
        order.lots = lots;
        order.modified = now;
    }

    public final void cancelOrder(Order order, long now) {
        removeOrder(order);
        order.cancel(now);
    }

    /**
     * Reduce residual lots by lots. If the resulting residual is zero, then the order is removed
     * from the side.
     */
    public final void takeOrder(Order order, long lots, long now) {
        assert order != null;
        assert order.level != null;

        reduce(order, lots);

        // Last trade.
        lastTicks = order.getTicks();
        lastLots = lots;
        lastTime = now;

        order.state = State.TRADE;
        order.exec += lots;
        order.lastTicks = lastTicks;
        order.lastLots = lastLots;
        order.modified = now;
    }

    public final DlNode getFirstOrder() {
        return orders.getFirst();
    }

    public final DlNode getLastOrder() {
        return orders.getLast();
    }

    public final boolean isEmptyOrder() {
        return orders.isEmpty();
    }

    public final Level findLevel(long id) {
        return (Level) levels.find(id);
    }

    public final RbNode getRootLevel() {
        return levels.getRoot();
    }

    public final RbNode getFirstLevel() {
        return levels.getFirst();
    }

    public final RbNode getLastLevel() {
        return levels.getLast();
    }

    public final boolean isEmptyLevel() {
        return levels.isEmpty();
    }

    public final long getLastTicks() {
        return lastTicks;
    }

    public final long getLastLots() {
        return lastLots;
    }

    public final long getLastTime() {
        return lastTime;
    }
}

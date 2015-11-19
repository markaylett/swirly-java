/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.intrusive.DlList;
import com.swirlycloud.twirly.node.DlNode;
import com.swirlycloud.twirly.node.RbNode;

public final class BookSide {

    private final LevelTree levels = new LevelTree();
    private final DlList orders = new DlList();

    private final Level getLazyLevel(Order order) {
        final long key = Level.composeKey(order.side, order.ticks);
        Level level = levels.pfind(key);
        if (level == null || level.getKey() != key) {
            final Level parent = level;
            level = new Level(order);
            levels.pinsert(level, parent);
        } else {
            level.addOrder(order);
        }
        order.level = level;
        return level;
    }

    private final void reduce(Order order, long delta) {
        assert order != null;
        assert order.level != null;
        assert delta >= 0 && delta <= order.resd;

        if (delta < order.resd) {
            // Reduce level and order by lots.
            final Level level = (Level) order.level;
            assert level != null;
            level.resd -= delta;
            order.resd -= delta;
        } else {
            assert delta == order.resd;
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
        assert order.ticks != 0;
        assert order.resd > 0;
        assert order.exec <= order.lots;
        assert order.lots > 0;
        assert order.minLots >= 0;

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
        assert level != null;
        level.resd -= order.resd;
        level.quotd -= order.quotd;

        if (--level.count == 0) {
            // Remove level.
            assert level.resd == 0;
            levels.remove(level);
        } else if (level.firstOrder == order) {
            // First order at this level is being removed.
            level.firstOrder = (Order) order.dlNext();
        }

        order.remove();

        // No longer associated with side.
        order.level = null;
    }

    public final void createOrder(Order order, long now) {
        order.create(now);
        insertOrder(order);
    }

    public final void reviseOrder(Order order, long lots, long now) {
        assert order != null;
        assert order.level != null;
        assert lots > 0;
        assert lots >= order.exec && lots >= order.minLots && lots <= order.lots;

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

        final long ticks = order.ticks;
        order.state = State.TRADE;
        order.exec += lots;
        order.cost += lots * ticks;
        order.lastLots = lots;
        order.lastTicks = ticks;
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

    public final @Nullable Level findLevel(long id) {
        return levels.find(id);
    }

    public final @Nullable RbNode getRootLevel() {
        return levels.getRoot();
    }

    public final @Nullable RbNode getFirstLevel() {
        return levels.getFirst();
    }

    public final @Nullable RbNode getLastLevel() {
        return levels.getLast();
    }

    public final boolean isEmptyLevel() {
        return levels.isEmpty();
    }
}

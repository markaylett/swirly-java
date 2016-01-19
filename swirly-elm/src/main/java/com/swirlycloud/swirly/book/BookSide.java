/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.book;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.entity.Order;
import com.swirlycloud.swirly.intrusive.DlList;
import com.swirlycloud.swirly.node.DlNode;
import com.swirlycloud.swirly.node.RbNode;

public final @NonNullByDefault class BookSide {

    private final LevelTree levels = new LevelTree();
    private final DlList orders = new DlList();

    /**
     * Insert level. This function will only throw if a new level cannot be allocated.
     */
    private final Level insertLevel(Order order) {
        final long key = Level.composeKey(order.getSide(), order.getTicks());
        Level level = levels.pfind(key);
        if (level == null || level.getKey() != key) {
            final Level parent = level;
            level = new Level(order);
            levels.pinsert(level, parent);
        } else {
            level.addOrder(order);
        }
        order.setLevel(level);
        return level;
    }

    private final void removeOrder(Level level, Order order) {

        level.subOrder(order);

        if (level.count == 0) {
            // Remove level.
            assert level.getResd() == 0;
            levels.remove(level);
        } else if (level.firstOrder == order) {
            // First order at this level is being removed.
            level.firstOrder = (Order) order.dlNext();
        }

        order.remove();

        // No longer associated with side.
        order.setLevel(null);
    }

    private final void reduceLevel(Level level, Order order, long delta) {
        assert delta >= 0;
        assert delta <= order.getResd();

        if (delta < order.getResd()) {
            // Reduce level's resd by delta.
            level.reduce(delta);
        } else {
            assert delta == order.getResd();
            removeOrder(level, order);
        }
    }

    /**
     * Insert order into side. Assumes that the order does not already belong to a side. I.e. it
     * assumes that level member is null. Assumes that order-id and reference are unique. This
     * function will only throw if a new level cannot be allocated.
     */
    public final void insertOrder(Order order) {

        assert order.getLevel() == null;
        assert order.getTicks() != 0;
        assert order.getResd() > 0;
        assert order.getExec() <= order.getLots();
        assert order.getLots() > 0;
        assert order.getMinLots() >= 0;

        final Level level = insertLevel(order);
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
     * Remove order from side. Internal housekeeping aside, the state of the order is not affected
     * by this function.
     */
    public final void removeOrder(Order order) {
        final Level level = (Level) order.getLevel();
        if (level != null) {
            removeOrder(level, order);
        }
    }

    public final void createOrder(Order order, long now) {
        order.create(now);
        insertOrder(order);
    }

    public final void reviseOrder(Order order, long lots, long now) {
        assert lots > 0;
        assert lots <= order.getLots();
        assert lots >= order.getExec();
        assert lots >= order.getMinLots();

        final Level level = (Level) order.getLevel();
        if (level != null) {
            final long delta = order.getLots() - lots;
            reduceLevel(level, order, delta);
        }
        order.revise(lots, now);
    }

    public final void cancelOrder(Order order, long now) {
        final Level level = (Level) order.getLevel();
        if (level != null) {
            removeOrder(level, order);
        }
        order.cancel(now);
    }

    /**
     * Reduce residual lots by lots. If the resulting residual is zero, then the order is removed
     * from the side.
     */
    public final void takeOrder(Order order, long lots, long now) {
        final Level level = (Level) order.getLevel();
        if (level != null) {
            reduceLevel(level, order, lots);
        }
        order.trade(lots, order.getTicks(), now);
    }

    public final DlNode getFirstOrder() {
        final DlNode node = orders.getFirst();
        assert node != null;
        return node;
    }

    public final DlNode getLastOrder() {
        final DlNode node = orders.getLast();
        assert node != null;
        return node;
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

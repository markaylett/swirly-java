/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.RefIdx;
import com.swirlycloud.domain.User;
import com.swirlycloud.util.BasicRbNode;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.RbNode;
import com.swirlycloud.util.Tree;

public final class Accnt extends BasicRbNode implements Identifiable {
    private final User user;
    private final RefIdx refIdx;
    private final Tree orders = new Tree();
    private final Tree trades = new Tree();
    private final Tree posns = new Tree();

    public Accnt(User user, RefIdx refIdx) {
        this.user = user;
        this.refIdx = refIdx;
    }

    @Override
    public final long getKey() {
        return user.getId();
    }

    @Override
    public final long getId() {
        return user.getId();
    }

    public final User getUser() {
        return user;
    }

    public final void insertOrder(Order order) {
        final RbNode node = orders.insert(order);
        assert node == order;
        if (!order.getRef().isEmpty())
            refIdx.insert(order);
    }

    public final void removeOrder(Order order) {
        assert user.getId() == order.getUserId();
        orders.remove(order);
        if (!order.getRef().isEmpty())
            refIdx.remove(user.getId(), order.getRef());
    }

    public final Order removeOrder(long id) {
        final RbNode node = orders.find(id);
        if (node == null)
            return null;
        final Order order = (Order) node;
        removeOrder(order);
        return order;
    }

    public final Order removeOrder(String ref) {
        final Order order = refIdx.remove(user.getId(), ref);
        if (order != null) {
            orders.remove(order);
        }
        return order;
    }

    public final Order findOrder(long id) {
        return (Order) orders.find(id);
    }

    /**
     * Returns order directly because hash lookup is not a node-based container.
     */

    public final Order findOrder(String ref) {
        assert ref != null && !ref.isEmpty();
        return refIdx.find(user.getId(), ref);
    }

    public final RbNode getFirstOrder() {
        return orders.getFirst();
    }

    public final RbNode getLastOrder() {
        return orders.getLast();
    }

    public final boolean isEmptyOrder() {
        return orders.isEmpty();
    }

    public final void insertTrade(Exec trade) {
        final RbNode node = trades.insert(trade);
        assert node == trade;
    }

    public final void removeTrade(Exec trade) {
        trades.remove(trade);
    }

    public final boolean removeTrade(long id) {
        final RbNode node = trades.find(id);
        if (node == null)
            return false;

        trades.remove(node);
        return true;
    }

    public final Exec findTrade(long id) {
        return (Exec) trades.find(id);
    }

    public final RbNode getFirstTrade() {
        return trades.getFirst();
    }

    public final RbNode getLastTrade() {
        return trades.getLast();
    }

    public final boolean isEmptyTrade() {
        return trades.isEmpty();
    }

    public final void insertPosn(Posn posn) {
        final RbNode node = posns.insert(posn);
        assert node == posn;
    }

    public final Posn updatePosn(Posn posn) {
        final RbNode node = posns.insert(posn);
        if (node != posn) {
            final Posn exist = (Posn) node;

            // Update existing position.

            assert exist.getUser().equals(posn.getUser());
            assert exist.getContr().equals(posn.getContr());
            assert exist.getSettlDay() == posn.getSettlDay();

            exist.setBuyLicks(posn.getBuyLicks());
            exist.setBuyLots(posn.getBuyLots());
            exist.setSellLicks(posn.getSellLicks());
            exist.setSellLots(posn.getSellLots());

            posn = exist;
        }
        return (Posn) node;
    }

    public final Posn getLazyPosn(Contr contr, int settlDay) {

        Posn posn;
        final long key = Posn.toSynthId(user.getId(), contr.getId(), settlDay);
        final RbNode node = posns.pfind(key);
        if (node == null || node.getKey() != key) {
            posn = new Posn(user, contr, settlDay);
            final RbNode parent = node;
            posns.pinsert(posn, parent);
        } else {
            posn = (Posn) node;
        }
        return posn;
    }

    public final Posn findPosn(Contr contr, int settlDay) {
        final long id = Posn.toSynthId(user.getId(), contr.getId(), settlDay);
        return (Posn) posns.find(id);
    }

    public final RbNode getFirstPosn() {
        return posns.getFirst();
    }

    public final RbNode getLastPosn() {
        return posns.getLast();
    }

    public final boolean isEmptyPosn() {
        return posns.isEmpty();
    }
}

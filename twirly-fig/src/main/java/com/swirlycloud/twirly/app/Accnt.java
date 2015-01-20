/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import com.swirlycloud.twirly.collection.BasicRbNode;
import com.swirlycloud.twirly.collection.RbNode;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.RefIdx;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.intrusive.RbTree;
import com.swirlycloud.twirly.util.Identifiable;

public final class Accnt extends BasicRbNode implements Identifiable {
    private final Trader trader;
    private final RefIdx refIdx;
    private final RbTree orders = new RbTree();
    private final RbTree trades = new RbTree();
    private final RbTree posns = new RbTree();

    public Accnt(Trader trader, RefIdx refIdx) {
        this.trader = trader;
        this.refIdx = refIdx;
    }

    @Override
    public final long getKey() {
        return trader.getId();
    }

    @Override
    public final long getId() {
        return trader.getId();
    }

    public final Trader getTrader() {
        return trader;
    }

    final void insertOrder(Order order) {
        final RbNode node = orders.insert(order);
        assert node == order;
        if (!order.getRef().isEmpty()) {
            refIdx.insert(order);
        }
    }

    final void removeOrder(Order order) {
        assert trader.getId() == order.getTraderId();
        orders.remove(order);
        if (!order.getRef().isEmpty()) {
            refIdx.remove(trader.getId(), order.getRef());
        }
    }

    final Order removeOrder(long contrId, int settlDay, long id) {
        final RbNode node = orders.find(Order.composeKey(contrId, settlDay, id));
        if (node == null) {
            return null;
        }
        final Order order = (Order) node;
        removeOrder(order);
        return order;
    }

    final Order removeOrder(String ref) {
        final Order order = refIdx.remove(trader.getId(), ref);
        if (order != null) {
            orders.remove(order);
        }
        return order;
    }

    public final Order findOrder(long contrId, int settlDay, long id) {
        return (Order) orders.find(Order.composeKey(contrId, settlDay, id));
    }

    /**
     * Returns order directly because hash lookup is not a node-based container.
     */

    public final Order findOrder(long contrId, int settlDay, String ref) {
        assert ref != null && !ref.isEmpty();
        return refIdx.find(trader.getId(), ref);
    }

    public final RbNode getRootOrder() {
        return orders.getRoot();
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

    final void insertTrade(Exec trade) {
        final RbNode node = trades.insert(trade);
        assert node == trade;
    }

    final void removeTrade(Exec trade) {
        trades.remove(trade);
    }

    final boolean removeTrade(long contrId, int settlDay, long id) {
        final RbNode node = trades.find(Exec.composeKey(contrId, settlDay, id));
        if (node == null) {
            return false;
        }

        trades.remove(node);
        return true;
    }

    public final Exec findTrade(long contrId, int settlDay, long id) {
        return (Exec) trades.find(Exec.composeKey(contrId, settlDay, id));
    }

    public final RbNode getRootTrade() {
        return trades.getRoot();
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

    final void insertPosn(Posn posn) {
        final RbNode node = posns.insert(posn);
        assert node == posn;
    }

    final Posn updatePosn(Posn posn) {
        final RbNode node = posns.insert(posn);
        if (node != posn) {
            final Posn exist = (Posn) node;

            // Update existing position.

            assert exist.getTrader().equals(posn.getTrader());
            assert exist.getContr().equals(posn.getContr());
            assert exist.getSettlDay() == posn.getSettlDay();

            exist.setBuyCost(posn.getBuyCost());
            exist.setBuyLots(posn.getBuyLots());
            exist.setSellCost(posn.getSellCost());
            exist.setSellLots(posn.getSellLots());

            posn = exist;
        }
        return (Posn) node;
    }

    final Posn getLazyPosn(Contr contr, int settlDay) {
        Posn posn;
        final long key = Posn.composeKey(contr.getId(), settlDay, trader.getId());
        final RbNode node = posns.pfind(key);
        if (node == null || node.getKey() != key) {
            posn = new Posn(trader, contr, settlDay);
            final RbNode parent = node;
            posns.pinsert(posn, parent);
        } else {
            posn = (Posn) node;
        }
        return posn;
    }

    public final Posn findPosn(Contr contr, int settlDay) {
        final long key = Posn.composeKey(contr.getId(), settlDay, trader.getId());
        return (Posn) posns.find(key);
    }

    public final RbNode getRootPosn() {
        return posns.getRoot();
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

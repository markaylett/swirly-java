/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.intrusive.BasicRbTree;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.RefHashTable;
import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.RbNode;

public final class Sess extends BasicRbNode {

    private static final class PosnTree extends BasicRbTree<String> {

        private static String getMarket(RbNode node) {
            return ((Posn) node).getMarket();
        }

        @Override
        protected final int compareKey(RbNode lhs, RbNode rhs) {
            return getMarket(lhs).compareTo(getMarket(rhs));
        }

        @Override
        protected final int compareKeyDirect(RbNode lhs, String rhs) {
            return getMarket(lhs).compareTo(rhs);
        }
    }

    private final Trader trader;
    private final RefHashTable refIdx;
    private final InstructTree orders = new InstructTree();
    private final InstructTree trades = new InstructTree();
    private final PosnTree posns = new PosnTree();

    public Sess(Trader trader, RefHashTable refIdx) {
        this.trader = trader;
        this.refIdx = refIdx;
    }

    public final String getTrader() {
        return trader.getMnem();
    }

    public final Trader getTraderRich() {
        return trader;
    }

    final void insertOrder(Order order) {
        final RbNode node = orders.insert(order);
        assert node == null;
        if (!order.getRef().isEmpty()) {
            refIdx.insert(order);
        }
    }

    final void removeOrder(Order order) {
        assert order.getTrader().equals(trader.getMnem());
        orders.remove(order);
        if (!order.getRef().isEmpty()) {
            refIdx.remove(trader.getMnem(), order.getRef());
        }
    }

    final Order removeOrder(String market, long id) {
        final RbNode node = orders.find(market, id);
        if (node == null) {
            return null;
        }
        final Order order = (Order) node;
        removeOrder(order);
        return order;
    }

    final Order removeOrder(String ref) {
        final Order order = (Order) refIdx.remove(trader.getMnem(), ref);
        if (order != null) {
            orders.remove(order);
        }
        return order;
    }

    public final Order findOrder(String market, long id) {
        return (Order) orders.find(market, id);
    }

    public final Order findOrder(String ref) {
        assert ref != null && !ref.isEmpty();
        return (Order) refIdx.find(trader.getMnem(), ref);
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
        assert node == null;
    }

    final void removeTrade(Exec trade) {
        trades.remove(trade);
    }

    final boolean removeTrade(String market, long id) {
        final RbNode node = trades.find(market, id);
        if (node == null) {
            return false;
        }
        trades.remove(node);
        return true;
    }

    public final Exec findTrade(String market, long id) {
        return (Exec) trades.find(market, id);
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
        assert node == null;
    }

    final Posn updatePosn(Posn posn) {
        final String market = posn.getMarket();
        final Posn exist = (Posn) posns.pfind(market);
        if (exist != null && exist.getMarket().equals(market)) {

            // Update existing position.

            assert exist.getTrader().equals(posn.getTrader());

            exist.setBuyCost(posn.getBuyCost());
            exist.setBuyLots(posn.getBuyLots());
            exist.setSellCost(posn.getSellCost());
            exist.setSellLots(posn.getSellLots());

            posn = exist;
        } else {
            final RbNode parent = exist;
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    final Posn getLazyPosn(Market market) {
        Posn posn = (Posn) posns.pfind(market.getMnem());
        if (posn == null || !posn.getMarket().equals(market.getMnem())) {
            final RbNode parent = posn;
            posn = new Posn(trader.getMnem(), market);
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    public final Posn findPosn(String market) {
        return (Posn) posns.find(market);
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

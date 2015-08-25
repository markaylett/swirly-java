/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.RefHashTable;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.node.RbNode;

public final @NonNullByDefault class TraderSess extends Trader {

    private static final long serialVersionUID = 1L;

    private final transient RefHashTable refIdx;
    private final transient Factory factory;
    private final transient InstructTree orders = new InstructTree();
    private final transient InstructTree trades = new InstructTree();
    private final transient TraderPosnTree posns = new TraderPosnTree();

    TraderSess(String mnem, @Nullable String display, String email, RefHashTable refIdx,
            Factory factory) {
        super(mnem, display, email);
        this.refIdx = refIdx;
        this.factory = factory;
    }

    final void insertOrder(Order order) {
        final RbNode unused = orders.insert(order);
        assert unused == null;
        if (order.getRef() != null) {
            refIdx.insert(order);
        }
    }

    final void removeOrder(Order order) {
        assert order.getTrader().equals(mnem);
        orders.remove(order);
        if (order.getRef() != null) {
            refIdx.remove(mnem, order.getRef());
        }
    }

    final @Nullable Order removeOrder(String market, long id) {
        final RbNode node = orders.find(market, id);
        if (node == null) {
            return null;
        }
        final Order order = (Order) node;
        removeOrder(order);
        return order;
    }

    final @Nullable Order removeOrder(String ref) {
        final Order order = (Order) refIdx.remove(mnem, ref);
        if (order != null) {
            orders.remove(order);
        }
        return order;
    }

    public final @Nullable Order findOrder(String market, long id) {
        return (Order) orders.find(market, id);
    }

    public final @Nullable Order findOrder(String ref) {
        assert ref != null;
        return (Order) refIdx.find(mnem, ref);
    }

    public final @Nullable RbNode getRootOrder() {
        return orders.getRoot();
    }

    public final @Nullable RbNode getFirstOrder() {
        return orders.getFirst();
    }

    public final @Nullable RbNode getLastOrder() {
        return orders.getLast();
    }

    public final boolean isEmptyOrder() {
        return orders.isEmpty();
    }

    final void insertTrade(Exec trade) {
        final RbNode unused = trades.insert(trade);
        assert unused == null;
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

    public final @Nullable Exec findTrade(String market, long id) {
        return (Exec) trades.find(market, id);
    }

    public final @Nullable RbNode getRootTrade() {
        return trades.getRoot();
    }

    public final @Nullable RbNode getFirstTrade() {
        return trades.getFirst();
    }

    public final @Nullable RbNode getLastTrade() {
        return trades.getLast();
    }

    public final boolean isEmptyTrade() {
        return trades.isEmpty();
    }

    final void insertPosn(Posn posn) {
        final RbNode unused = posns.insert(posn);
        assert unused == null;
    }

    final Posn addPosn(Posn posn) {
        final String contr = posn.getContr();
        final int settlDay = posn.getSettlDay();
        final Posn exist = (Posn) posns.pfind(contr, settlDay);
        if (exist != null && exist.getContr().equals(contr) && exist.getSettlDay() == settlDay) {
            // Add to existing position.
            exist.add(posn);
            posn = exist;
        } else {
            final RbNode parent = exist;
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    final Posn getLazyPosn(Market market) {
        Posn posn = (Posn) posns.pfind(market.getContr(), market.getSettlDay());
        if (posn == null || !posn.getContr().equals(market.getContr())
                || posn.getSettlDay() != market.getSettlDay()) {
            final RbNode parent = posn;
            posn = factory.newPosn(mnem, market.getContr(), market.getSettlDay());
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    public final @Nullable Posn findPosn(String contr, int settlDay) {
        return (Posn) posns.find(contr, settlDay);
    }

    public final @Nullable RbNode getRootPosn() {
        return posns.getRoot();
    }

    public final @Nullable RbNode getFirstPosn() {
        return posns.getFirst();
    }

    public final @Nullable RbNode getLastPosn() {
        return posns.getLast();
    }

    public final boolean isEmptyPosn() {
        return posns.isEmpty();
    }

    final void settlPosns(int busDay) {
        for (RbNode node = posns.getFirst(); node != null;) {
            final Posn posn = (Posn) node;
            node = node.rbNext();
            if (posn.isSettlDaySet() && posn.getSettlDay() <= busDay) {
                posns.remove(posn);
                posn.setSettlDay(0);
                addPosn(posn);
            }
        }
    }
}

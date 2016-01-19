/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.io.Cache;
import com.swirlycloud.swirly.node.RbNode;
import com.swirlycloud.swirly.node.SlNode;

public @NonNullByDefault class TraderSess extends Trader implements SlNode {

    private static final long serialVersionUID = 1L;

    // Dirty bits.
    public static final int DIRTY_EMAIL = 1 << 0;
    public static final int DIRTY_ORDER = 1 << 1;
    public static final int DIRTY_TRADE = 1 << 2;
    public static final int DIRTY_POSN = 1 << 3;
    public static final int DIRTY_QUOTE = 1 << 4;
    public static final int DIRTY_ALL = DIRTY_EMAIL | DIRTY_ORDER | DIRTY_TRADE | DIRTY_POSN
            | DIRTY_QUOTE;

    // Used by EmailIdx.
    private transient @Nullable SlNode slNext;

    private final transient RequestRefMap refIdx;
    private final transient Factory factory;
    final transient RequestIdTree orders = new RequestIdTree();
    final transient RequestIdTree trades = new RequestIdTree();
    final transient TraderPosnTree posns = new TraderPosnTree();
    final transient RequestIdTree quotes = new RequestIdTree();
    @Nullable
    private transient TraderSess dirtyNext;
    private transient int dirty;

    protected TraderSess(String mnem, @Nullable String display, String email, RequestRefMap refIdx,
            Factory factory) {
        super(mnem, display, email);
        this.refIdx = refIdx;
        this.factory = factory;
    }

    @Override
    public final void setSlNext(@Nullable SlNode next) {
        this.slNext = next;
    }

    @Override
    public final @Nullable SlNode slNext() {
        return slNext;
    }

    public final void insertOrder(Order order) {
        assert order.getTrader().equals(mnem);
        final RbNode unused = orders.insert(order);
        assert unused == null;
        if (order.getRef() != null) {
            refIdx.insert(order);
        }
    }

    public final void removeOrder(Order order) {
        assert order.getTrader().equals(mnem);
        orders.remove(order);
        if (order.getRef() != null) {
            refIdx.remove(mnem, order.getRef());
        }
    }

    public final @Nullable Order findOrder(String market, long id) {
        return (Order) orders.find(market, id);
    }

    public final @Nullable Order findOrder(String ref) {
        return (Order) refIdx.find(mnem, ref);
    }

    public final @Nullable Order getRootOrder() {
        return (Order) orders.getRoot();
    }

    public final @Nullable Order getFirstOrder() {
        return (Order) orders.getFirst();
    }

    public final @Nullable Order getLastOrder() {
        return (Order) orders.getLast();
    }

    public final boolean isEmptyOrder() {
        return orders.isEmpty();
    }

    public final void insertTrade(Exec trade) {
        final RbNode unused = trades.insert(trade);
        assert unused == null;
    }

    public final void removeTrade(Exec trade) {
        trades.remove(trade);
    }

    public final @Nullable Exec findTrade(String market, long id) {
        return (Exec) trades.find(market, id);
    }

    public final @Nullable Exec getRootTrade() {
        return (Exec) trades.getRoot();
    }

    public final @Nullable Exec getFirstTrade() {
        return (Exec) trades.getFirst();
    }

    public final @Nullable Exec getLastTrade() {
        return (Exec) trades.getLast();
    }

    public final boolean isEmptyTrade() {
        return trades.isEmpty();
    }

    public final void insertPosn(Posn posn) {
        final RbNode unused = posns.insert(posn);
        assert unused == null;
    }

    public final Posn addPosn(Posn posn) {
        final String contr = posn.getContr();
        final int settlDay = posn.getSettlDay();
        final Posn exist = posns.pfind(contr, settlDay);
        if (exist != null && exist.getContr().equals(contr) && exist.getSettlDay() == settlDay) {
            // Add to existing position.
            exist.add(posn);
            posn = exist;
        } else {
            final Posn parent = exist;
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    public final Posn getLazyPosn(String contr, int settlDay) {
        Posn posn = posns.pfind(contr, settlDay);
        if (posn == null || !posn.getContr().equals(contr) || posn.getSettlDay() != settlDay) {
            final Posn parent = posn;
            posn = factory.newPosn(mnem, contr, settlDay);
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    public final @Nullable Posn findPosn(String contr, int settlDay) {
        return posns.find(contr, settlDay);
    }

    public final @Nullable Posn getRootPosn() {
        return posns.getRoot();
    }

    public final @Nullable Posn getFirstPosn() {
        return posns.getFirst();
    }

    public final @Nullable Posn getLastPosn() {
        return posns.getLast();
    }

    public final boolean isEmptyPosn() {
        return posns.isEmpty();
    }

    public final void insertQuote(Quote quote) {
        final RbNode unused = quotes.insert(quote);
        assert unused == null;
    }

    public final void removeQuote(Quote quote) {
        quotes.remove(quote);
    }

    public final @Nullable Quote findQuote(String market, long id) {
        return (Quote) quotes.find(market, id);
    }

    public final @Nullable Quote getRootQuote() {
        return (Quote) quotes.getRoot();
    }

    public final @Nullable Quote getFirstQuote() {
        return (Quote) quotes.getFirst();
    }

    public final @Nullable Quote getLastQuote() {
        return (Quote) quotes.getLast();
    }

    public final boolean isEmptyQuote() {
        return quotes.isEmpty();
    }

    /**
     * Settle positions.
     * 
     * @param busDay
     * @return returns the number of positions modified.
     */
    public final int settlPosns(int busDay) {
        int modified = 0;
        for (RbNode node = posns.getFirst(); node != null;) {
            final Posn posn = (Posn) node;
            node = node.rbNext();
            if (posn.isSettlDaySet() && posn.getSettlDay() <= busDay) {
                posns.remove(posn);
                posn.setSettlDay(0);
                addPosn(posn);
                ++modified;
            }
        }
        return modified;
    }

    public static TraderSess insertDirty(@Nullable final TraderSess first, TraderSess next,
            int dirty) {

        next.dirty |= dirty;

        if (first == null) {
            next.dirtyNext = null; // Defensive.
            return next;
        }

        TraderSess node = first;
        for (;;) {
            assert node != null;
            if (node == next) {
                // Entry already exists.
                break;
            } else if (node.dirtyNext == null) {
                next.dirtyNext = null; // Defensive.
                node.dirtyNext = next;
                break;
            }
            node = node.dirtyNext;
        }
        return first;
    }

    public final @Nullable TraderSess popDirty() {
        final TraderSess next = dirtyNext;
        dirtyNext = null;
        return next;
    }

    public final void updateCache(Cache cache) {
        if ((dirty & DIRTY_EMAIL) != 0) {
            cache.update("trader:" + email, mnem);
            // Reset flag on success.
            dirty &= ~DIRTY_EMAIL;
        }
        if ((dirty & DIRTY_ORDER) != 0) {
            cache.update("order:" + mnem, orders);
            // Reset flag on success.
            dirty &= ~DIRTY_ORDER;
        }
        if ((dirty & DIRTY_TRADE) != 0) {
            cache.update("trade:" + mnem, trades);
            // Reset flag on success.
            dirty &= ~DIRTY_TRADE;
        }
        if ((dirty & DIRTY_POSN) != 0) {
            cache.update("posn:" + mnem, posns);
            // Reset flag on success.
            dirty &= ~DIRTY_POSN;
        }
        if ((dirty & DIRTY_QUOTE) != 0) {
            cache.update("quote:" + mnem, quotes);
            // Reset flag on success.
            dirty &= ~DIRTY_QUOTE;
        }
    }
}

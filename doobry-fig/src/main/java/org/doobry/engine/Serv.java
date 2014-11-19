/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Action;
import org.doobry.domain.Contr;
import org.doobry.domain.Direct;
import org.doobry.domain.Exec;
import org.doobry.domain.Kind;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.domain.RefIdx;
import org.doobry.domain.Role;
import org.doobry.domain.Side;
import org.doobry.domain.User;
import org.doobry.util.DlNode;
import org.doobry.util.RbNode;
import org.doobry.util.SlNode;
import org.doobry.util.Tree;

public final class Serv implements AutoCloseable {
    private static int CACHE_BUCKETS = 257;
    private static int REFIDX_BUCKETS = 257;
    private final Model model;
    private final Cache cache = new Cache(CACHE_BUCKETS);
    private final RefIdx refIdx = new RefIdx(REFIDX_BUCKETS);
    private final Tree books = new Tree();
    private final Tree accnts = new Tree();

    private final void enrichOrder(Order order) {
        final User user = (User) cache.findRec(Kind.USER, order.getUserId());
        final Contr contr = (Contr) cache.findRec(Kind.CONTR, order.getContrId());
        order.enrich(user, contr);
    }

    private final void enrichTrade(Exec trade) {
        final User user = (User) cache.findRec(Kind.USER, trade.getUserId());
        final Contr contr = (Contr) cache.findRec(Kind.CONTR, trade.getContrId());
        final User cpty = (User) cache.findRec(Kind.USER, trade.getCptyId());
        trade.enrich(user, contr, cpty);
    }

    private final void enrichPosn(Posn posn) {
        final User user = (User) cache.findRec(Kind.USER, posn.getUserId());
        final Contr contr = (Contr) cache.findRec(Kind.CONTR, posn.getContrId());
        posn.enrich(user, contr);
    }

    private final void insertRecList(Kind kind) {
        cache.insertList(kind, model.selectRec(kind));
    }

    private final void insertOrders() {
        for (final Order order : model.selectOrder()) {
            enrichOrder(order);
            final Book book = getLazyBook(order.getContr(), order.getSettlDay());
            book.insertOrder(order);
            boolean success = false;
            try {
                final Accnt accnt = getLazyAccnt(order.getUser());
                accnt.insertOrder(order);
                success = true;
            } finally {
                if (!success) {
                    book.removeOrder(order);
                }
            }
        }
    }

    private final void insertTrades() {
        for (final Exec trade : model.selectTrade()) {
            enrichTrade(trade);
            final Accnt accnt = getLazyAccnt(trade.getUser());
            accnt.insertTrade(trade);
        }
    }

    private final void insertPosns() {
        for (final Posn posn : model.selectPosn()) {
            enrichPosn(posn);
            final Accnt accnt = getLazyAccnt(posn.getUser());
            accnt.insertPosn(posn);
        }
    }

    private final Exec newExec(Order order, long now) {
        final long execId = model.allocIds(Kind.EXEC, 1);
        return new Exec(execId, order.getId(), order, now);
    }

    private static long spread(Order takerOrder, Order makerOrder, Direct direct) {
        return direct == Direct.PAID
        // Paid when the taker lifts the offer.
        ? makerOrder.getTicks() - takerOrder.getTicks()
                // Given when the taker hits the bid.
                : takerOrder.getTicks() - makerOrder.getTicks();
    }

    private final void matchOrders(Book book, Order takerOrder, Side side, Direct direct,
            Trans trans) {

        final long now = takerOrder.getCreated();

        long taken = 0;
        long lastTicks = 0;
        long lastLots = 0;

        final Contr contr = book.getContr();
        final int settlDay = book.getSettlDay();

        DlNode node = side.getFirstOrder();
        for (; taken < takerOrder.getResd() && !node.isEnd(); node = node.dlNext()) {
            final Order makerOrder = (Order) node;

            // Only consider orders while prices cross.
            if (spread(takerOrder, makerOrder, direct) > 0)
                break;

            final long makerId = model.allocIds(Kind.EXEC, 2);
            final long takerId = makerId + 1;

            final Accnt makerAccnt = getLazyAccnt(makerOrder.getUser());
            final Posn makerPosn = makerAccnt.getLazyPosn(contr, settlDay);

            final Match match = new Match();
            match.makerOrder = makerOrder;
            match.makerPosn = makerPosn;
            match.ticks = makerOrder.getTicks();
            match.lots = Math.min(takerOrder.getResd() - taken, makerOrder.getResd());

            taken += match.lots;
            lastTicks = match.ticks;
            lastLots = match.lots;

            final Exec makerTrade = new Exec(makerId, makerOrder.getId(), makerOrder, now);
            makerTrade.trade(match.lots, match.ticks, match.lots, takerId, Role.MAKER,
                    takerOrder.getUser());
            match.makerTrade = makerTrade;

            final Exec takerTrade = new Exec(takerId, takerOrder.getId(), takerOrder, now);
            takerTrade.trade(taken, match.ticks, match.lots, makerId, Role.TAKER,
                    makerOrder.getUser());
            match.takerTrade = takerTrade;

            trans.matches.insertBack(match);

            // Maker updated first because this is consistent with last-look semantics.
            // N.B. the reference count is not incremented here.
            trans.execs.insertBack(makerTrade);
            trans.execs.insertBack(takerTrade);
        }

        if (!trans.matches.isEmpty()) {
            // Avoid allocating position when there are no matches.
            final Accnt takerAccnt = getLazyAccnt(takerOrder.getUser());
            trans.posn = takerAccnt.getLazyPosn(contr, settlDay);
            takerOrder.trade(taken, lastTicks, lastLots, now);
        }
    }

    public final void matchOrders(Book book, Order taker, Trans trans) {
        Side side;
        Direct direct;
        if (taker.getAction() == Action.BUY) {
            // Paid when the taker lifts the offer.
            side = book.getOfferSide();
            direct = Direct.PAID;
        } else {
            assert taker.getAction() == Action.SELL;
            // Given when the taker hits the bid.
            side = book.getBidSide();
            direct = Direct.GIVEN;
        }
        matchOrders(book, taker, side, direct, trans);
    }

    // Assumes that maker lots have not been reduced since matching took place.

    private final void commitMatches(Accnt taker, Book book, long now, Trans trans) {
        for (SlNode node = trans.matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            final Order makerOrder = match.getMakerOrder();
            // Reduce maker.
            book.takeOrder(makerOrder, match.getLots(), now);
            // Must succeed because maker order exists.
            final Accnt maker = getLazyAccnt(makerOrder.getUser());
            if (makerOrder.isDone()) {
                maker.removeOrder(makerOrder);
            }
            // Maker updated first because this is consistent with last-look semantics.
            // Update maker.
            maker.insertTrade(match.makerTrade);
            match.makerPosn.applyTrade(match.makerTrade);
            // Update taker.
            taker.insertTrade(match.takerTrade);
            trans.posn.applyTrade(match.takerTrade);
        }
    }

    public Serv(Model model) {
        this.model = model;
        insertRecList(Kind.ASSET);
        insertRecList(Kind.CONTR);
        insertRecList(Kind.USER);
        insertOrders();
        insertTrades();
        insertPosns();
    }

    @Override
    public final void close() {
    }

    public final Rec findRec(Kind kind, long id) {
        return cache.findRec(kind, id);
    }

    public final Rec findRec(Kind kind, String mnem) {
        return cache.findRec(kind, mnem);
    }

    public final SlNode getFirstRec(Kind kind) {
        return cache.getFirstRec(kind);
    }

    public final boolean isEmptyRec(Kind kind) {
        return cache.isEmptyRec(kind);
    }

    public final Book getLazyBook(Contr contr, int settlDay) {
        Book book;
        final long id = Book.toId(contr.getId(), settlDay);
        final RbNode node = books.pfind(id);
        if (node == null || node.getId() != id) {
            book = new Book(contr, settlDay);
            final RbNode parent = node;
            books.pinsert(book, parent);
        } else {
            book = (Book) node;
        }
        return book;
    }

    public final Book getLazyBook(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRec(Kind.CONTR, mnem);
        if (contr == null) {
            throw new IllegalArgumentException(String.format("invalid contr '%s'", mnem));
        }
        return getLazyBook(contr, settlDay);
    }

    public final Book findBook(Contr contr, int settlDay) {
        return (Book) books.find(Book.toId(contr.getId(), settlDay));
    }

    public final Book findBook(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRec(Kind.CONTR, mnem);
        if (contr == null) {
            throw new IllegalArgumentException(String.format("invalid contr '%s'", mnem));
        }
        return findBook(contr, settlDay);
    }

    public final RbNode getFirstBook() {
        return books.getFirst();
    }

    public final RbNode getLastBook() {
        return books.getLast();
    }

    public final boolean isEmptyBook() {
        return books.isEmpty();
    }

    public final Accnt getLazyAccnt(User user) {
        Accnt accnt;
        final long id = user.getId();
        final RbNode node = accnts.pfind(id);
        if (node == null || node.getId() != id) {
            accnt = new Accnt(user, refIdx);
            final RbNode parent = node;
            accnts.pinsert(accnt, parent);
        } else {
            accnt = (Accnt) node;
        }
        return accnt;
    }

    public final Accnt getLazyAccnt(String mnem) {
        final User user = (User) cache.findRec(Kind.USER, mnem);
        if (user == null) {
            throw new IllegalArgumentException(String.format("invalid user '%s'", mnem));
        }
        return getLazyAccnt(user);
    }
    
    public final Trans placeOrder(Accnt accnt, Book book, String ref, Action action, long ticks,
            long lots, long minLots, Trans trans) {
        if (lots == 0 || lots < minLots) {
            throw new IllegalArgumentException(String.format("invalid lots '%d'", lots));
        }
        final long now = System.currentTimeMillis();
        final long orderId = model.allocIds(Kind.ORDER, 1);
        final Contr contr = book.getContr();
        final int settlDay = book.getSettlDay();
        final Order order = new Order(orderId, accnt.getUser(), contr, settlDay, ref, action,
                ticks, lots, minLots, now);
        final Exec exec = newExec(order, now);

        trans.clear();
        trans.order = order;
        trans.execs.insertBack(exec);
        // Order fields are updated on match.
        matchOrders(book, order, trans);
        // Place incomplete order in book.
        if (!order.isDone()) {
            // This may fail if level cannot be allocated.
            book.insertOrder(order);
        }
        // TODO: IOC orders would need an additional revision for the unsolicited cancellation of
        // any unfilled quantity.
        boolean success = false;
        try {
            model.insertExecList((Exec) trans.execs.getFirst());
            success = true;
        } finally {
            if (!success && !order.isDone()) {
                // Undo book insertion.
                book.removeOrder(order);
            }
        }
        // Final commit phase cannot fail.
        if (!order.isDone()) {
            accnt.insertOrder(order);
        }
        // Commit trans to cycle and free matches.
        commitMatches(accnt, book, now, trans);
        return trans;
    }

    public final Trans reviseOrder(Accnt accnt, Order order, long lots, Trans trans) {
        if (order.isDone()) {
            throw new IllegalArgumentException(String.format("order complete '%d'", order.getId()));
        }
        // Revised lots must not be:
        // 1. less than min lots;
        // 2. less than executed lots;
        // 3. greater than original lots.
        if (lots == 0 || lots < order.getMinLots() || lots < order.getExec()
                || lots > order.getLots()) {
            throw new IllegalArgumentException(String.format("invalid lots '%d'", lots));
        }
        final long now = System.currentTimeMillis();
        final Exec exec = newExec(order, now);
        exec.revise(lots);
        model.insertExec(exec);

        // Final commit phase cannot fail.
        final Book book = findBook(order.getContr(), order.getSettlDay());
        assert book != null;
        book.reviseOrder(order, lots, now);

        trans.clear();
        trans.order = order;
        trans.execs.insertBack(exec);
        return trans;
    }

    public final Trans reviseOrder(Accnt accnt, long id, long lots, Trans trans) {
        final Order order = accnt.findOrder(id);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%d'", id));
        }
        return reviseOrder(accnt, order, lots, trans);
    }

    public final Trans reviseOrder(Accnt accnt, String ref, long lots, Trans trans) {
        final Order order = accnt.findOrder(ref);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%s'", ref));
        }
        return reviseOrder(accnt, order, lots, trans);
    }

    public final Trans cancelOrder(Accnt accnt, Order order, Trans trans) {
        if (order.isDone()) {
            throw new IllegalArgumentException(String.format("order complete '%d'", order.getId()));
        }
        final long now = System.currentTimeMillis();
        final Exec exec = newExec(order, now);
        exec.cancel();
        model.insertExec(exec);

        // Final commit phase cannot fail.
        final Book book = findBook(order.getContr(), order.getSettlDay());
        assert book != null;
        book.cancelOrder(order, now);
        accnt.removeOrder(order);

        trans.clear();
        trans.order = order;
        trans.execs.insertBack(exec);
        return trans;
    }

    public final Trans cancelOrder(Accnt accnt, long id, Trans trans) {
        final Order order = accnt.findOrder(id);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%d'", id));
        }
        return cancelOrder(accnt, order, trans);
    }

    public final Trans cancelOrder(Accnt accnt, String ref, Trans trans) {
        final Order order = accnt.findOrder(ref);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%s'", ref));
        }
        return cancelOrder(accnt, order, trans);
    }

    public final void confirmTrade(Accnt accnt, long id) {
        final Exec trade = accnt.findTrade(id);
        if (trade == null) {
            throw new IllegalArgumentException(String.format("no such trade '%d'", id));
        }
        final long now = System.currentTimeMillis();
        model.updateExec(id, now);

        // No need to update timestamps on trade because it is immediately freed.
        accnt.removeTrade(trade);
    }
}

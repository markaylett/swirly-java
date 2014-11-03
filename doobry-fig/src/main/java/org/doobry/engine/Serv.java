/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Action;
import org.doobry.domain.Contr;
import org.doobry.domain.Exec;
import org.doobry.domain.Journ;
import org.doobry.domain.Model;
import org.doobry.domain.RefIdx;
import org.doobry.domain.Order;
import org.doobry.domain.User;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.domain.RecType;
import org.doobry.domain.Reg;
import org.doobry.util.Bank;
import org.doobry.util.Queue;
import org.doobry.util.RbNode;
import org.doobry.util.SlNode;
import org.doobry.util.Tree;

public final class Serv implements AutoCloseable {
    private static int CACHE_BUCKETS = 257;
    private static int REFIDX_BUCKETS = 257;
    private final Bank bank;
    private final Journ journ;
    private final Cache cache = new Cache(CACHE_BUCKETS);
    private final RefIdx refIdx = new RefIdx(REFIDX_BUCKETS);
    private final Tree books = new Tree();
    private final Queue execs = new Queue();

    private final void enrichOrder(Order order) {
        final User user = (User) cache.findRecId(RecType.USER, order.getUserId());
        final Contr contr = (Contr) cache.findRecId(RecType.CONTR, order.getContrId());
        order.enrich(user, contr);
    }

    private final void enrichTrade(Exec trade) {
        final User user = (User) cache.findRecId(RecType.USER, trade.getUserId());
        final Contr contr = (Contr) cache.findRecId(RecType.CONTR, trade.getContrId());
        final User cpty = (User) cache.findRecId(RecType.USER, trade.getCptyId());
        trade.enrich(user, contr, cpty);
    }

    private final void enrichPosn(Posn posn) {
        final User user = (User) cache.findRecId(RecType.USER, posn.getUserId());
        final Contr contr = (Contr) cache.findRecId(RecType.CONTR, posn.getContrId());
        posn.enrich(user, contr);
    }

    private final void insertRecList(Model model, RecType type) {
        cache.insertList(type, model.readRec(type));
    }

    private final void insertOrders(Model model) {
        for (final Order order : model.readOrder()) {
            enrichOrder(order);
            final Accnt user = Accnt.getLazyAccnt(order.getUser(), refIdx);
            user.insertOrder(order);
        }
    }

    private final void insertTrades(Model model) {
        for (final Exec trade : model.readTrade()) {
            enrichTrade(trade);
            final Accnt user = Accnt.getLazyAccnt(trade.getUser(), refIdx);
            user.insertTrade(trade);
        }
    }

    private final void insertPosns(Model model) {
        for (final Posn posn : model.readPosn()) {
            enrichPosn(posn);
            final Accnt accnt = Accnt.getLazyAccnt(posn.getUser(), refIdx);
            accnt.insertPosn(posn);
        }
    }

    private final Exec newExec(Order order, long now) {
        final long execId = bank.addFetch(Reg.EXEC_ID.intValue(), 1);
        return new Exec(execId, order.getId(), order, now);
    }

    // Assumes that maker lots have not been reduced since matching took place.

    private final void commitTrans(Accnt taker, Book book, Trans trans, long now) {
        for (SlNode node = trans.matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            final Order makerOrder = match.getMakerOrder();
            // Reduce maker.
            book.takeOrder(makerOrder, match.getLots(), now);
            // Must succeed because maker order exists.
            final Accnt maker = Accnt.getLazyAccnt(makerOrder.getUser(), refIdx);
            // Maker updated first because this is consistent with last-look semantics.
            // Update maker.
            maker.insertTrade(match.makerTrade);
            match.makerPosn.applyTrade(match.makerTrade);
            // Update taker.
            taker.insertTrade(match.takerTrade);
            trans.takerPosn.applyTrade(match.takerTrade);
        }
        execs.join(trans.execs);
    }

    public Serv(Bank bank, Journ journ) {
        this.bank = bank;
        this.journ = journ;
    }

    @Override
    public final void close() {
    }

    public final void load(Model model) {
        insertRecList(model, RecType.ASSET);
        insertRecList(model, RecType.CONTR);
        insertRecList(model, RecType.USER);
        insertOrders(model);
        insertTrades(model);
        insertPosns(model);
    }

    public final Rec findRecId(RecType type, long id) {
        return cache.findRecId(type, id);
    }

    public final Rec findRecMnem(RecType type, String mnem) {
        return cache.findRecMnem(type, mnem);
    }

    public final SlNode getFirstRec(RecType type) {
        return cache.getFirstRec(type);
    }

    public final boolean isEmptyRec(RecType type) {
        return cache.isEmptyRec(type);
    }

    public final Accnt getLazyAccnt(User user) {
        return Accnt.getLazyAccnt(user, refIdx);
    }

    public final Accnt getLazyAccnt(String mnem) {
        final User user = (User) cache.findRecMnem(RecType.USER, mnem);
        if (user == null) {
            throw new IllegalArgumentException(String.format("invalid user '%s'", mnem));
        }
        return getLazyAccnt(user);
    }

    public final Order placeOrder(Accnt user, Book book, String ref, Action action, long ticks,
            long lots, long minLots) {
        if (lots == 0 || lots < minLots) {
            throw new IllegalArgumentException(String.format("invalid lots '%d'", lots));
        }
        final long now = System.currentTimeMillis();
        final long orderId = bank.addFetch(Reg.ORDER_ID.intValue(), 1);
        final Contr contr = book.getContr();
        final int settlDay = book.getSettlDay();
        final Order order = new Order(orderId, user.getUser(), contr, settlDay, ref, action, ticks,
                lots, minLots, now);
        final Exec newExec = newExec(order, now);
        final Trans trans = new Trans();
        trans.execs.insertBack(newExec);
        // Order fields are updated on match.
        Broker.matchOrders(book, order, bank, refIdx, trans);
        // Place incomplete order in book.
        if (!order.isDone()) {
            book.insertOrder(order);
        }
        // TODO: IOC orders would need an additional revision for the unsolicited cancellation of
        // any unfilled quantity.
        boolean success = false;
        try {
            journ.insertExecList((Exec) trans.execs.getFirst());
            success = true;
        } finally {
            if (!success && !order.isDone()) {
                book.removeOrder(order);
            }
        }
        // Final commit phase cannot fail.
        user.insertOrder(order);
        // Commit trans to cycle and free matches.
        commitTrans(user, book, trans, now);
        return order;
    }

    public final void reviseOrder(Accnt user, Order order, long lots) {
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
        journ.insertExec(exec);

        // Final commit phase cannot fail.
        final Book book = findBook(order.getContr(), order.getSettlDay());
        assert book != null;
        book.reviseOrder(order, lots, now);
        execs.insertBack(exec);
    }

    public final Order reviseOrderId(Accnt user, long id, long lots) {
        final Order order = user.findOrderId(id);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%d'", id));
        }
        reviseOrder(user, order, lots);
        return order;
    }

    public final Order reviseOrderRef(Accnt user, String ref, long lots) {
        final Order order = user.findOrderRef(ref);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%s'", ref));
        }
        reviseOrder(user, order, lots);
        return order;
    }

    public final void cancelOrder(Accnt user, Order order) {
        if (order.isDone()) {
            throw new IllegalArgumentException(String.format("order complete '%d'", order.getId()));
        }
        final long now = System.currentTimeMillis();
        final Exec exec = newExec(order, now);
        exec.cancel();
        journ.insertExec(exec);

        // Final commit phase cannot fail.
        final Book book = findBook(order.getContr(), order.getSettlDay());
        assert book != null;
        book.cancelOrder(order, now);
        execs.insertBack(exec);
    }

    public final Order cancelOrderId(Accnt user, long id) {
        final Order order = user.findOrderId(id);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%d'", id));
        }
        cancelOrder(user, order);
        return order;
    }

    public final Order cancelOrderRef(Accnt user, String ref) {
        final Order order = user.findOrderRef(ref);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%s'", ref));
        }
        cancelOrder(user, order);
        return order;
    }

    public final void ackTrade(Accnt user, long id) {
        final Exec trade = user.findTradeId(id);
        if (trade != null) {
            throw new IllegalArgumentException(String.format("no such trade '%d'", id));
        }
        final long now = System.currentTimeMillis();
        journ.updateExec(id, now);

        // No need to update timestamps on trade because it is immediately freed.

        user.removeTrade(trade);
    }

    public final void clear() {
        while (!execs.isEmpty()) {
            final Exec exec = (Exec) execs.removeFirst();
            if (exec.isDone()) {
                final User user = exec.getUser();
                final Accnt accnt = (Accnt) user.getAccnt();
                assert accnt != null;
                accnt.releaseOrderId(exec.getOrder());
            }
        }
    }

    public final SlNode getFirstExec() {
        return execs.getFirst();
    }

    public final boolean isEmptyExec() {
        return execs.isEmpty();
    }

    public final Book getLazyBook(Contr contr, int settlDay) {
        Book book;
        final long key = Book.toKey(contr.getId(), settlDay);
        final RbNode node = books.pfind(key);
        if (node == null || node.getKey() != key) {
            book = new Book(contr, settlDay);
            final RbNode parent = node;
            books.pinsert(book, parent);
        } else {
            book = (Book) node;
        }
        return book;
    }

    public final Book getLazyBook(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRecMnem(RecType.CONTR, mnem);
        if (contr == null) {
            throw new IllegalArgumentException(String.format("invalid contr '%s'", mnem));
        }
        return getLazyBook(contr, settlDay);
    }

    public final Book findBook(Contr contr, int settlDay) {
        return (Book) books.find(Book.toKey(contr.getId(), settlDay));
    }

    public final Book findBook(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRecMnem(RecType.CONTR, mnem);
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
}

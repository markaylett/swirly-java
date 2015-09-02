/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeJdToIso;
import static com.swirlycloud.twirly.node.SlUtil.popNext;

import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.intrusive.EmailHashTable;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.Journ;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.DlNode;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;

public @NonNullByDefault class Serv {

    private static final int CAPACITY = 1 << 5; // 64
    @SuppressWarnings("null")
    private static final Pattern MNEM_PATTERN = Pattern.compile("^[0-9A-Za-z-._]{3,16}$");

    private final Journ journ;
    private final Cache cache;
    private final Factory factory;
    private final MnemRbTree assets;
    private final MnemRbTree contrs;
    private final MnemRbTree markets;
    private final MnemRbTree views = new MnemRbTree();
    private final MnemRbTree traders;
    private final EmailHashTable emailIdx = new EmailHashTable(CAPACITY);
    @Nullable
    private MarketBook dirtyBook;
    @Nullable
    private TraderSess dirtySess;

    private final void enrichContr(Contr contr) {
        final Asset asset = (Asset) assets.find(contr.getAsset());
        final Asset ccy = (Asset) assets.find(contr.getCcy());
        assert asset != null;
        assert ccy != null;
        contr.enrich(asset, ccy);
    }

    private final void enrichMarket(Market market) {
        final Contr contr = (Contr) contrs.find(market.getContr());
        assert contr != null;
        market.enrich(contr);
    }

    private final void insertOrder(Order order) {
        final TraderSess sess = (TraderSess) traders.find(order.getTrader());
        assert sess != null;
        sess.insertOrder(order);
        if (!order.isDone()) {
            final MarketBook book = (MarketBook) markets.find(order.getMarket());
            boolean success = false;
            try {
                assert book != null;
                book.insertOrder(order);
                success = true;
            } finally {
                if (!success) {
                    sess.removeOrder(order);
                }
            }
        }
    }

    private final void enrichContrs() {
        for (RbNode node = contrs.getFirst(); node != null; node = node.rbNext()) {
            final Contr contr = (Contr) node;
            enrichContr(contr);
        }
    }

    private final void enrichMarkets() {
        for (RbNode node = markets.getFirst(); node != null; node = node.rbNext()) {
            final MarketBook book = (MarketBook) node;
            enrichMarket(book);
            views.insert(book.view);
            setDirty(book, MarketBook.DIRTY_VIEW);
        }
    }

    private final void updateEmailIdx() {
        for (RbNode node = traders.getFirst(); node != null; node = node.rbNext()) {
            final Trader trader = (Trader) node;
            emailIdx.insert(trader);
            cache.update("trader:" + trader.getEmail(), trader.getMnem());
        }
    }

    private final void insertOrders(@Nullable SlNode first) {
        for (SlNode node = first; node != null;) {
            final Order order = (Order) node;
            node = popNext(node);

            insertOrder(order);
        }
    }

    private final void insertTrades(@Nullable SlNode first) {
        for (SlNode node = first; node != null;) {
            final Exec trade = (Exec) node;
            node = popNext(node);

            final TraderSess sess = (TraderSess) traders.find(trade.getTrader());
            assert sess != null;
            sess.insertTrade(trade);
        }
    }

    private final void insertPosns(@Nullable SlNode first) {
        for (SlNode node = first; node != null;) {
            final Posn posn = (Posn) node;
            node = popNext(node);

            final TraderSess sess = (TraderSess) traders.find(posn.getTrader());
            assert sess != null;
            sess.insertPosn(posn);
        }
    }

    private final TraderSess newTrader(String mnem, String display, String email)
            throws BadRequestException {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new BadRequestException(String.format("invalid mnem '%s'", mnem));
        }
        return (TraderSess) factory.newTrader(mnem, display, email);
    }

    private final MarketBook newMarket(String mnem, String display, Contr contr, int settlDay,
            int expiryDay, int state) throws BadRequestException {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new BadRequestException(String.format("invalid mnem '%s'", mnem));
        }
        return (MarketBook) factory.newMarket(mnem, display, contr, settlDay, expiryDay, state);
    }

    private final Exec newExec(MarketBook book, Instruct instruct, long now) {
        return factory.newExec(book.allocExecId(), instruct, now);
    }

    private static long spread(Order takerOrder, Order makerOrder, Direct direct) {
        return direct == Direct.PAID
        // Paid when the taker lifts the offer.
        ? makerOrder.getTicks() - takerOrder.getTicks()
                // Given when the taker hits the bid.
                : takerOrder.getTicks() - makerOrder.getTicks();
    }

    private final void matchOrders(TraderSess takerSess, MarketBook book, Order takerOrder,
            BookSide side, Direct direct, Trans trans) {

        final long now = takerOrder.getCreated();

        long takenLots = 0;
        long takenCost = 0;
        long lastTicks = 0;
        long lastLots = 0;

        DlNode node = side.getFirstOrder();
        for (; takenLots < takerOrder.getResd() && !node.isEnd(); node = node.dlNext()) {
            final Order makerOrder = (Order) node;

            // Only consider orders while prices cross.
            if (spread(takerOrder, makerOrder, direct) > 0) {
                break;
            }

            final long makerId = book.allocExecId();
            final long takerId = book.allocExecId();

            final TraderSess makerSess = (TraderSess) traders.find(makerOrder.getTrader());
            assert makerSess != null;
            final Posn makerPosn = makerSess.getLazyPosn(book);

            final Match match = new Match();
            match.makerOrder = makerOrder;
            match.makerPosn = makerPosn;
            match.ticks = makerOrder.getTicks();
            match.lots = Math.min(takerOrder.getResd() - takenLots, makerOrder.getResd());

            takenLots += match.lots;
            takenCost += match.lots * match.ticks;
            lastTicks = match.ticks;
            lastLots = match.lots;

            final Exec makerTrade = factory.newExec(makerId, makerOrder, now);
            makerTrade.trade(match.ticks, match.lots, takerId, Role.MAKER, takerOrder.getTrader());
            match.makerTrade = makerTrade;

            final Exec takerTrade = factory.newExec(takerId, takerOrder, now);
            takerTrade.trade(takenLots, takenCost, match.ticks, match.lots, makerId, Role.TAKER,
                    makerOrder.getTrader());
            match.takerTrade = takerTrade;

            trans.matches.insertBack(match);

            // Maker updated first because this is consistent with last-look semantics.
            // N.B. the reference count is not incremented here.
            trans.execs.insertBack(makerTrade);
            trans.execs.insertBack(takerTrade);
        }

        if (!trans.matches.isEmpty()) {
            // Avoid allocating position when there are no matches.
            trans.posn = takerSess.getLazyPosn(book);
            takerOrder.trade(takenLots, takenCost, lastTicks, lastLots, now);
        }
    }

    private final void matchOrders(TraderSess sess, MarketBook book, Order order, Trans trans) {
        BookSide side;
        Direct direct;
        if (order.getSide() == Side.BUY) {
            // Paid when the taker lifts the offer.
            side = book.getOfferSide();
            direct = Direct.PAID;
        } else {
            assert order.getSide() == Side.SELL;
            // Given when the taker hits the bid.
            side = book.getBidSide();
            direct = Direct.GIVEN;
        }
        matchOrders(sess, book, order, side, direct, trans);
    }

    private final void doCancelOrders(MarketBook book, long now) throws NotFoundException,
            ServiceUnavailableException {
        final BookSide bidSide = book.getBidSide();
        final BookSide offerSide = book.getOfferSide();

        SlNode firstExec = null;
        for (DlNode node = bidSide.getFirstOrder(); !node.isEnd(); node = node.dlNext()) {
            final Order order = (Order) node;
            final Exec exec = newExec(book, order, now);
            exec.cancel();
            // Stack push.
            exec.setSlNext(firstExec);
            firstExec = exec;
        }
        for (DlNode node = offerSide.getFirstOrder(); !node.isEnd(); node = node.dlNext()) {
            final Order order = (Order) node;
            final Exec exec = newExec(book, order, now);
            exec.cancel();
            // Stack push.
            exec.setSlNext(firstExec);
            firstExec = exec;
        }
        try {
            journ.insertExecList(book.getMnem(), firstExec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(book, MarketBook.DIRTY_VIEW);

        for (DlNode node = bidSide.getFirstOrder(); !node.isEnd();) {
            final Order order = (Order) node;
            node = node.dlNext();
            bidSide.cancelOrder(order, now);

            final TraderSess sess = (TraderSess) traders.find(order.getTrader());
            assert sess != null;
            setDirty(sess, TraderSess.DIRTY_ORDER);
        }

        for (DlNode node = offerSide.getFirstOrder(); !node.isEnd();) {
            final Order order = (Order) node;
            node = node.dlNext();
            offerSide.cancelOrder(order, now);

            final TraderSess sess = (TraderSess) traders.find(order.getTrader());
            assert sess != null;
            setDirty(sess, TraderSess.DIRTY_ORDER);
        }
    }

    private final void doArchiveOrders(TraderSess sess, long now) throws NotFoundException,
            ServiceUnavailableException {

        // Set dirty here, because the block below may throw.
        setDirty(sess, TraderSess.DIRTY_ORDER);

        RbNode node = sess.getFirstOrder();
        while (node != null) {
            final Order order = (Order) node;
            // Move to next node before this order is unlinked.
            node = node.rbNext();
            if (!order.isDone()) {
                continue;
            }
            try {
                journ.archiveOrder(order.getMarket(), order.getId(), now);
            } catch (final RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // No need to update timestamps on order because it is immediately freed.
            sess.removeOrder(order);
        }
    }

    private final void doArchiveTrades(TraderSess sess, long now) throws NotFoundException,
            ServiceUnavailableException {

        // Set dirty here, because the block below may throw.
        setDirty(sess, TraderSess.DIRTY_TRADE);

        for (;;) {
            final Exec trade = (Exec) sess.getRootTrade();
            if (trade == null) {
                break;
            }
            try {
                journ.archiveTrade(trade.getMarket(), trade.getId(), now);
            } catch (final RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // No need to update timestamps on trade because it is immediately freed.
            sess.removeTrade(trade);
        }
    }

    private final void flushDirty() {

        while (dirtySess != null) {
            final TraderSess sess = dirtySess;
            assert sess != null;
            sess.flushDirty(cache);
            // Pop if flush succeeded.
            dirtySess = sess.dirtyNext;
            sess.dirtyNext = null;
        }

        if (dirtyBook != null) {

            do {
                final MarketBook book = dirtyBook;
                assert book != null;
                book.flushDirty();
                // Pop if flush succeeded.
                dirtyBook = book.dirtyNext;
                book.dirtyNext = null;
            } while (dirtyBook != null);

            // At least one view has changed.
            cache.update("view", views);
        }
    }

    private final void setDirty(MarketBook next, int dirty) {

        next.setDirty(dirty);

        MarketBook book = dirtyBook;
        if (book == null) {
            next.dirtyNext = null; // Defensive.
            dirtyBook = next;
        } else {
            for (;;) {
                assert book != null;
                if (book == next) {
                    // Entry already exists.
                    break;
                } else if (book.dirtyNext == null) {
                    next.dirtyNext = null; // Defensive.
                    book.dirtyNext = next;
                    break;
                }
                book = book.dirtyNext;
            }
        }
    }

    private final void setDirty(TraderSess next, int dirty) {

        next.setDirty(dirty);

        TraderSess sess = dirtySess;
        if (sess == null) {
            next.dirtyNext = null; // Defensive.
            dirtySess = next;
        } else {
            for (;;) {
                assert sess != null;
                if (sess == next) {
                    // Entry already exists.
                    break;
                } else if (sess.dirtyNext == null) {
                    next.dirtyNext = null; // Defensive.
                    sess.dirtyNext = next;
                    break;
                }
                sess = sess.dirtyNext;
            }
        }
    }

    // Assumes that maker lots have not been reduced since matching took place.

    private final void commitMatches(TraderSess taker, MarketBook book, long now, Trans trans) {
        setDirty(book, MarketBook.DIRTY_VIEW);
        SlNode node = trans.matches.getFirst();
        if (node == null) {
            // There are no matches.
            setDirty(taker, TraderSess.DIRTY_ORDER);
            return;
        }
        setDirty(taker, TraderSess.DIRTY_ORDER | TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);
        do {
            final Match match = (Match) node;
            final Order makerOrder = match.getMakerOrder();
            assert makerOrder != null;
            // Reduce maker.
            book.takeOrder(makerOrder, match.getLots(), now);
            // Must succeed because maker order exists.
            final TraderSess maker = (TraderSess) traders.find(makerOrder.getTrader());
            assert maker != null;
            // Maker updated first because this is consistent with last-look semantics.
            // Update maker.
            final Exec makerTrade = match.makerTrade;
            assert makerTrade != null;
            maker.insertTrade(makerTrade);
            match.makerPosn.addTrade(makerTrade);
            // Update taker.
            final Exec takerTrade = match.takerTrade;
            assert takerTrade != null;
            taker.insertTrade(takerTrade);
            trans.posn.addTrade(takerTrade);
            setDirty(maker, TraderSess.DIRTY_ORDER | TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);
            // Next match.
            node = node.slNext();
        } while (node != null);
    }

    public Serv(Model model, Journ journ, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this.journ = journ;
        this.cache = cache;
        this.factory = factory;

        MnemRbTree t = model.selectAsset(factory);
        assert t != null;
        this.assets = t;
        cache.update("asset", t);

        t = model.selectContr(factory);
        assert t != null;
        this.contrs = t;
        enrichContrs();
        cache.update("contr", t);

        t = model.selectMarket(factory);
        assert t != null;
        this.markets = t;
        enrichMarkets();
        cache.update("market", t);

        t = model.selectTrader(factory);
        assert t != null;
        this.traders = t;
        updateEmailIdx();
        cache.update("trader", t);

        insertOrders(model.selectOrder(factory));
        insertTrades(model.selectTrade(factory));
        insertPosns(model.selectPosn(getBusDate(now).toJd(), factory));

        for (RbNode node = traders.getFirst(); node != null; node = node.rbNext()) {
            final TraderSess sess = (TraderSess) node;
            setDirty(sess, TraderSess.DIRTY_ALL);
        }
        flushDirty();
    }

    public Serv(Datastore datastore, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this(datastore, datastore, cache, factory, now);
    }

    public final TraderSess createTrader(String mnem, String display, String email)
            throws BadRequestException, ServiceUnavailableException {
        if (traders.find(mnem) != null) {
            throw new BadRequestException(String.format("trader '%s' already exists", mnem));
        }
        if (emailIdx.find(email) != null) {
            throw new BadRequestException(String.format("email '%s' is already in use", email));
        }
        final TraderSess sess = newTrader(mnem, display, email);
        try {
            journ.insertTrader(mnem, display, email);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }
        traders.insert(sess);
        emailIdx.insert(sess);
        cache.update("trader", traders);
        cache.update("trader:" + email, mnem);
        setDirty(sess, TraderSess.DIRTY_ORDER | TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);
        flushDirty();
        return sess;
    }

    public final TraderSess updateTrader(String mnem, String display) throws BadRequestException,
            NotFoundException, ServiceUnavailableException {
        final TraderSess sess = (TraderSess) traders.find(mnem);
        if (sess == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", mnem));
        }
        sess.setDisplay(display);
        try {
            journ.updateTrader(mnem, display);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }
        cache.update("trader", traders);
        return sess;
    }

    public final @Nullable Rec findRec(RecType recType, String mnem) {
        Rec ret = null;
        switch (recType) {
        case ASSET:
            ret = (Rec) assets.find(mnem);
            break;
        case CONTR:
            ret = (Rec) contrs.find(mnem);
            break;
        case MARKET:
            ret = (Rec) markets.find(mnem);
            break;
        case TRADER:
            ret = (Rec) traders.find(mnem);
            break;
        }
        return ret;
    }

    public final @Nullable RbNode getRootRec(RecType recType) {
        RbNode ret = null;
        switch (recType) {
        case ASSET:
            ret = assets.getRoot();
            break;
        case CONTR:
            ret = contrs.getRoot();
            break;
        case MARKET:
            ret = markets.getRoot();
            break;
        case TRADER:
            ret = traders.getRoot();
            break;
        }
        return ret;
    }

    public final @Nullable RbNode getFirstRec(RecType recType) {
        RbNode ret = null;
        switch (recType) {
        case ASSET:
            ret = assets.getFirst();
            break;
        case CONTR:
            ret = contrs.getFirst();
            break;
        case MARKET:
            ret = markets.getFirst();
            break;
        case TRADER:
            ret = traders.getFirst();
            break;
        }
        return ret;
    }

    public final @Nullable RbNode getLastRec(RecType recType) {
        RbNode ret = null;
        switch (recType) {
        case ASSET:
            ret = assets.getLast();
            break;
        case CONTR:
            ret = contrs.getLast();
            break;
        case MARKET:
            ret = markets.getLast();
            break;
        case TRADER:
            ret = traders.getLast();
            break;
        }
        return ret;
    }

    public final boolean isEmptyRec(RecType recType) {
        boolean ret = true;
        switch (recType) {
        case ASSET:
            ret = assets.isEmpty();
            break;
        case CONTR:
            ret = contrs.isEmpty();
            break;
        case MARKET:
            ret = markets.isEmpty();
            break;
        case TRADER:
            ret = traders.isEmpty();
            break;
        }
        return ret;
    }

    public final MarketBook getMarket(String mnem) throws NotFoundException {
        final MarketBook book = (MarketBook) markets.find(mnem);
        if (book == null) {
            throw new NotFoundException(String.format("market '%s' does not exist", mnem));
        }
        return book;
    }

    public final TraderSess getTrader(String mnem) throws NotFoundException {
        final TraderSess sess = (TraderSess) traders.find(mnem);
        if (sess == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", mnem));
        }
        return sess;
    }

    public final @Nullable TraderSess findTraderByEmail(String email) {
        return (TraderSess) emailIdx.find(email);
    }

    public final MarketBook createMarket(String mnem, String display, Contr contr, int settlDay,
            int expiryDay, int state, long now) throws BadRequestException,
            ServiceUnavailableException {
        if (settlDay != 0) {
            // busDay <= expiryDay <= settlDay.
            final int busDay = getBusDate(now).toJd();
            if (settlDay < expiryDay) {
                throw new BadRequestException("settl-day before expiry-day");
            }
            if (expiryDay < busDay) {
                throw new BadRequestException("expiry-day before bus-day");
            }
        } else {
            if (expiryDay != 0) {
                throw new BadRequestException("expiry-day without settl-day");
            }
        }
        MarketBook book = (MarketBook) markets.pfind(mnem);
        if (book != null && book.getMnem().equals(mnem)) {
            throw new BadRequestException(String.format("market '%s' already exists", mnem));
        }
        final RbNode parent = book;
        book = newMarket(mnem, display, contr, settlDay, expiryDay, state);
        try {
            journ.insertMarket(mnem, display, contr.getMnem(), settlDay, expiryDay, state);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        markets.pinsert(book, parent);
        views.insert(book.view);
        cache.update("market", markets);
        return book;
    }

    public final MarketBook createMarket(String mnem, String display, String contrMnem,
            int settlDay, int expiryDay, int state, long now) throws BadRequestException,
            NotFoundException, ServiceUnavailableException {
        final Contr contr = (Contr) contrs.find(contrMnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contr '%s' does not exist", contrMnem));
        }
        return createMarket(mnem, display, contr, settlDay, expiryDay, state, now);
    }

    public final MarketBook updateMarket(String mnem, String display, int state, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final MarketBook book = (MarketBook) markets.find(mnem);
        if (book == null) {
            throw new NotFoundException(String.format("market '%s' does not exist", mnem));
        }
        book.setDisplay(display);
        book.setState(state);
        try {
            journ.updateMarket(mnem, display, state);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }
        cache.update("market", markets);
        return book;
    }

    public final void expireMarkets(long now) throws NotFoundException, ServiceUnavailableException {
        final int busDay = getBusDate(now).toJd();
        for (RbNode node = markets.getFirst(); node != null;) {
            final MarketBook book = (MarketBook) node;
            node = node.rbNext();
            if (book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
                doCancelOrders(book, now);
            }
        }
        flushDirty();
    }

    public final void settlMarkets(long now) {
        final int busDay = getBusDate(now).toJd();
        for (RbNode node = markets.getFirst(); node != null;) {
            final MarketBook book = (MarketBook) node;
            node = node.rbNext();
            if (book.isSettlDaySet() && book.getSettlDay() <= busDay) {
                views.remove(book.view);
                markets.remove(book);
            }
        }
        for (RbNode node = traders.getFirst(); node != null; node = node.rbNext()) {
            final TraderSess sess = (TraderSess) node;
            if (sess.settlPosns(busDay) > 0) {
                setDirty(sess, TraderSess.DIRTY_POSN);
            }
        }
        flushDirty();
    }

    public final void placeOrder(TraderSess sess, MarketBook book, @Nullable String ref, Side side,
            long ticks, long lots, long minLots, long now, Trans trans) throws BadRequestException,
            NotFoundException, ServiceUnavailableException {
        final int busDay = getBusDate(now).toJd();
        if (book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
            throw new NotFoundException(String.format("market for '%s' on '%d' has expired", book
                    .getContrRich().getMnem(), maybeJdToIso(book.getSettlDay())));
        }
        if (lots == 0 || lots < minLots) {
            throw new BadRequestException(String.format("invalid lots '%d'", lots));
        }
        final long orderId = book.allocOrderId();
        final Order order = factory.newOrder(orderId, sess.getMnem(), book, ref, side, ticks, lots,
                minLots, now);
        final Exec exec = newExec(book, order, now);

        trans.reset(book, order, exec);
        // Order fields are updated on match.
        matchOrders(sess, book, order, trans);
        // Place incomplete order in market.
        if (!order.isDone()) {
            // This may fail if level cannot be allocated.
            book.insertOrder(order);
        }
        // TODO: IOC orders would need an additional revision for the unsolicited cancellation of
        // any unfilled quantity.
        boolean success = false;
        try {
            final SlNode first = trans.prepareExecList();
            journ.insertExecList(book.getMnem(), first);
            success = true;
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        } finally {
            if (!success && !order.isDone()) {
                // Undo market insertion.
                book.removeOrder(order);
            }
        }
        // Final commit phase cannot fail.
        sess.insertOrder(order);
        // Commit trans to cycle and free matches.
        commitMatches(sess, book, now, trans);
        flushDirty();
    }

    public final void reviseOrder(TraderSess sess, MarketBook book, Order order, long lots,
            long now, Trans trans) throws BadRequestException, NotFoundException,
            ServiceUnavailableException {
        if (order.isDone()) {
            throw new BadRequestException(String.format("order '%d' is done", order.getId()));
        }
        // Revised lots must not be:
        // 1. less than min lots;
        // 2. less than executed lots;
        // 3. greater than original lots.
        if (lots == 0 || lots < order.getMinLots() || lots < order.getExec()
                || lots > order.getLots()) {
            throw new BadRequestException(String.format("invalid lots '%d'", lots));
        }

        final Exec exec = newExec(book, order, now);
        exec.revise(lots);
        try {
            journ.insertExec(exec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Final commit phase cannot fail.
        book.reviseOrder(order, lots, now);

        trans.reset(book, order, exec);
        setDirty(book, MarketBook.DIRTY_VIEW);
        setDirty(sess, TraderSess.DIRTY_ORDER);
        flushDirty();
    }

    public final void reviseOrder(TraderSess sess, MarketBook book, long id, long lots, long now,
            Trans trans) throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(book.getMnem(), id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        reviseOrder(sess, book, order, lots, now, trans);
    }

    public final void reviseOrder(TraderSess sess, MarketBook book, String ref, long lots,
            long now, Trans trans) throws BadRequestException, NotFoundException,
            ServiceUnavailableException {
        final Order order = sess.findOrder(ref);
        if (order == null) {
            throw new NotFoundException(String.format("order '%s' does not exist", ref));
        }
        reviseOrder(sess, book, order, lots, now, trans);
    }

    public final void cancelOrder(TraderSess sess, MarketBook book, Order order, long now,
            Trans trans) throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (order.isDone()) {
            throw new BadRequestException(String.format("order '%d' is done", order.getId()));
        }
        final Exec exec = newExec(book, order, now);
        exec.cancel();
        try {
            journ.insertExec(exec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Final commit phase cannot fail.
        book.cancelOrder(order, now);

        trans.reset(book, order, exec);
        setDirty(book, MarketBook.DIRTY_VIEW);
        setDirty(sess, TraderSess.DIRTY_ORDER);
        flushDirty();
    }

    public final void cancelOrder(TraderSess sess, MarketBook book, long id, long now, Trans trans)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(book.getMnem(), id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        cancelOrder(sess, book, order, now, trans);
    }

    public final void cancelOrder(TraderSess sess, MarketBook book, String ref, long now,
            Trans trans) throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(ref);
        if (order == null) {
            throw new NotFoundException(String.format("order '%s' does not exist", ref));
        }
        cancelOrder(sess, book, order, now, trans);
    }

    /**
     * Cancels all orders.
     * 
     * This method is not executed atomically, so it may partially fail.
     * 
     * @param sess
     *            The session.
     * @param now
     *            The current time.
     * @throws NotFoundException
     * @throws ServiceUnavailableException
     */
    public final void cancelOrders(TraderSess sess, long now) throws NotFoundException,
            ServiceUnavailableException {
        for (;;) {
            final Order order = (Order) sess.getRootOrder();
            if (order == null) {
                break;
            }
            final MarketBook book = (MarketBook) markets.find(order.getMarket());
            assert book != null;

            final Exec exec = newExec(book, order, now);
            exec.cancel();
            try {
                journ.insertExec(exec);
            } catch (final RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // Final commit phase cannot fail.
            setDirty(book, MarketBook.DIRTY_VIEW);
            book.cancelOrder(order, now);
        }
        setDirty(sess, TraderSess.DIRTY_ORDER);
        flushDirty();
    }

    public final void cancelOrders(MarketBook book, long now) throws NotFoundException,
            ServiceUnavailableException {
        doCancelOrders(book, now);
        flushDirty();
    }

    public final void archiveOrder(TraderSess sess, Order order, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (!order.isDone()) {
            throw new BadRequestException(String.format("order '%d' is not done", order.getId()));
        }
        try {
            journ.archiveOrder(order.getMarket(), order.getId(), now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // No need to update timestamps on order because it is immediately freed.
        sess.removeOrder(order);

        setDirty(sess, TraderSess.DIRTY_ORDER);
        flushDirty();
    }

    public final void archiveOrder(TraderSess sess, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(market, id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        archiveOrder(sess, order, now);
    }

    /**
     * Archive all orders.
     * 
     * This method is not updated atomically, so it may partially fail.
     * 
     * @param sess
     *            The session.
     * @param now
     *            The current time.
     * @throws NotFoundException
     * @throws ServiceUnavailableException
     */
    public final void archiveOrders(TraderSess sess, long now) throws NotFoundException,
            ServiceUnavailableException {
        doArchiveOrders(sess, now);
        flushDirty();
    }

    public final Exec createTrade(TraderSess sess, MarketBook book, String ref, Side side,
            long ticks, long lots, @Nullable Role role, @Nullable String cpty, long created)
            throws NotFoundException, ServiceUnavailableException {
        final Posn posn = sess.getLazyPosn(book);
        final Exec trade = Exec.manual(book.allocExecId(), sess.getMnem(), book.getMnem(),
                book.getContr(), book.getSettlDay(), ref, side, ticks, lots, role, cpty, created);
        if (cpty != null) {
            // Create back-to-back trade if counter-party is specified.
            final TraderSess cptySess = (TraderSess) traders.find(cpty);
            if (cptySess == null) {
                throw new NotFoundException(String.format("cpty '%s' does not exist", cpty));
            }
            final Posn cptyPosn = cptySess.getLazyPosn(book);
            final Exec cptyTrade = trade.inverse(book.allocExecId());
            trade.setSlNext(cptyTrade);
            try {
                journ.insertExecList(book.getMnem(), trade);
            } catch (final RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }
            sess.insertTrade(trade);
            posn.addTrade(trade);
            cptySess.insertTrade(cptyTrade);
            cptyPosn.addTrade(cptyTrade);
            // Update counter-party cache entries.
            setDirty(cptySess, TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);
        } else {
            try {
                journ.insertExec(trade);
            } catch (final RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }
            sess.insertTrade(trade);
            posn.addTrade(trade);
        }
        setDirty(sess, TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);
        flushDirty();
        return trade;
    }

    public final void archiveTrade(TraderSess sess, Exec trade, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (trade.getState() != State.TRADE) {
            throw new BadRequestException(String.format("exec '%d' is not a trade", trade.getId()));
        }
        try {
            journ.archiveTrade(trade.getMarket(), trade.getId(), now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // No need to update timestamps on trade because it is immediately freed.
        sess.removeTrade(trade);
        setDirty(sess, TraderSess.DIRTY_TRADE);
        flushDirty();
    }

    public final void archiveTrade(TraderSess sess, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Exec trade = sess.findTrade(market, id);
        if (trade == null) {
            throw new NotFoundException(String.format("trade '%d' does not exist", id));
        }
        archiveTrade(sess, trade, now);
    }

    /**
     * Archive all trades.
     * 
     * This method is not executed atomically, so it may partially fail.
     * 
     * @param sess
     *            The session.
     * @param now
     *            The current time.
     * @throws NotFoundException
     * @throws ServiceUnavailableException
     */
    public final void archiveTrades(TraderSess sess, long now) throws NotFoundException,
            ServiceUnavailableException {
        doArchiveTrades(sess, now);
        flushDirty();
    }

    public final void archiveAll(TraderSess sess, long now) throws NotFoundException,
            ServiceUnavailableException {
        doArchiveOrders(sess, now);
        doArchiveTrades(sess, now);
        flushDirty();
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeJdToIso;
import static com.swirlycloud.twirly.node.SlUtil.popNext;
import static com.swirlycloud.twirly.util.CollectionUtil.compareLong;

import java.util.Comparator;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.collection.PriorityQueue;
import com.swirlycloud.twirly.domain.BookSide;
import com.swirlycloud.twirly.domain.Direct;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Instruct;
import com.swirlycloud.twirly.domain.MarketBook;
import com.swirlycloud.twirly.domain.MarketId;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Quote;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.domain.TraderSess;
import com.swirlycloud.twirly.exception.AlreadyExistsException;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.InvalidException;
import com.swirlycloud.twirly.exception.InvalidLotsException;
import com.swirlycloud.twirly.exception.MarketClosedException;
import com.swirlycloud.twirly.exception.MarketNotFoundException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.OrderNotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.exception.TooLateException;
import com.swirlycloud.twirly.exception.TraderNotFoundException;
import com.swirlycloud.twirly.intrusive.MarketViewTree;
import com.swirlycloud.twirly.intrusive.RecTree;
import com.swirlycloud.twirly.intrusive.TraderEmailMap;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.Journ;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.DlNode;
import com.swirlycloud.twirly.node.JslNode;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.rec.Asset;
import com.swirlycloud.twirly.rec.Contr;
import com.swirlycloud.twirly.rec.Market;
import com.swirlycloud.twirly.rec.Rec;
import com.swirlycloud.twirly.rec.RecType;

public @NonNullByDefault class Serv {

    private static final int CAPACITY = 1 << 5; // 64

    // Dirty bits.
    private static final int DIRTY_ASSET = 1 << 0;
    private static final int DIRTY_CONTR = 1 << 1;
    private static final int DIRTY_MARKET = 1 << 2;
    private static final int DIRTY_TRADER = 1 << 3;
    private static final int DIRTY_VIEW = 1 << 5;
    private static final int DIRTY_TIMEOUT = 1 << 6;
    private static final int DIRTY_ALL = DIRTY_ASSET | DIRTY_CONTR | DIRTY_MARKET | DIRTY_TRADER
            | DIRTY_VIEW | DIRTY_TIMEOUT;

    @SuppressWarnings("null")
    private static final Pattern MNEM_PATTERN = Pattern.compile("^[0-9A-Za-z-._]{3,16}$");

    // 30 seconds.
    private static final int QUOTE_EXPIRY = 30 * 1000;

    private final Journ journ;
    private final Cache cache;
    private final Factory factory;
    private final RecTree assets;
    private final RecTree contrs;
    private final RecTree markets;
    private final RecTree traders;
    private final MarketViewTree views = new MarketViewTree();
    private final TraderEmailMap emailIdx = new TraderEmailMap(CAPACITY);
    private final PriorityQueue<Quote> quotes = new PriorityQueue<>(new Comparator<Quote>() {
        @Override
        public final int compare(@Nullable Quote lhs, @Nullable Quote rhs) {
            assert lhs != null;
            assert rhs != null;
            return compareLong(lhs.getExpiry(), rhs.getExpiry());
        }
    });
    private transient int dirty;
    @Nullable
    private TraderSess dirtySess;
    @Nullable
    private MarketBook dirtyBook;

    private final void setDirty(int dirty) {
        this.dirty |= dirty;
    }

    private final void setDirty(TraderSess next, int dirty) {
        dirtySess = TraderSess.insertDirty(dirtySess, next, dirty);
    }

    private final void setDirty(MarketBook next) {
        dirtyBook = MarketBook.insertDirty(dirtyBook, next);
        // Implies dirty view.
        dirty |= DIRTY_VIEW;
    }

    private final void updateDirty() {

        if ((dirty & DIRTY_ASSET) != 0) {
            cache.update("asset", assets);
            // Reset flag on success.
            dirty &= ~DIRTY_ASSET;
        }

        if ((dirty & DIRTY_CONTR) != 0) {
            cache.update("contr", contrs);
            // Reset flag on success.
            dirty &= ~DIRTY_CONTR;
        }

        if ((dirty & DIRTY_MARKET) != 0) {
            cache.update("market", markets);
            // Reset flag on success.
            dirty &= ~DIRTY_MARKET;
        }

        if ((dirty & DIRTY_TRADER) != 0) {
            cache.update("trader", traders);
            // Reset flag on success.
            dirty &= ~DIRTY_TRADER;
        }

        if ((dirty & DIRTY_TIMEOUT) != 0) {
            cache.update("timeout", getTimeout());
            // Reset flag on success.
            dirty &= ~DIRTY_TIMEOUT;
        }

        while (dirtyBook != null) {
            final MarketBook book = dirtyBook;
            assert book != null;
            book.updateView();
            // Pop if flush succeeded.
            dirtyBook = book.popDirty();
        }

        while (dirtySess != null) {
            final TraderSess sess = dirtySess;
            assert sess != null;
            sess.updateCache(cache);
            // Pop if flush succeeded.
            dirtySess = sess.popDirty();
        }

        // Must happen after book has been updated above.
        if ((dirty & DIRTY_VIEW) != 0) {
            cache.update("view", views);
            // Reset flag on success.
            dirty &= ~DIRTY_VIEW;
        }
    }

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
            views.insert(book.getView());
        }
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
                setDirty(book);
                success = true;
            } finally {
                if (!success) {
                    sess.removeOrder(order);
                }
            }
        }
    }

    private final TraderSess newTrader(String mnem, @Nullable String display, String email)
            throws BadRequestException {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new InvalidException(String.format("invalid mnem '%s'", mnem));
        }
        return (TraderSess) factory.newTrader(mnem, display, email);
    }

    private final MarketBook newMarket(String mnem, @Nullable String display, Contr contr,
            int settlDay, int expiryDay, int state) throws BadRequestException {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new InvalidException(String.format("invalid mnem '%s'", mnem));
        }
        return (MarketBook) factory.newMarket(mnem, display, contr, settlDay, expiryDay, state);
    }

    private final Exec newExec(MarketBook book, Instruct instruct, long now) {
        return factory.newExec(instruct, book.allocExecId(), now);
    }

    private static long spread(Order takerOrder, Order makerOrder, Direct direct) {
        return direct == Direct.PAID
                // Paid when the taker lifts the offer.
                ? makerOrder.getTicks() - takerOrder.getTicks()
                // Given when the taker hits the bid.
                : takerOrder.getTicks() - makerOrder.getTicks();
    }

    private final void matchOrders(TraderSess takerSess, MarketBook book, Order takerOrder,
            BookSide side, Direct direct, Result result) {

        final long now = takerOrder.getCreated();

        long takenLots = 0;
        long takenCost = 0;
        long lastLots = 0;
        long lastTicks = 0;

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
            match.lots = Math.min(takerOrder.getResd() - takenLots, makerOrder.getAvail());

            takenLots += match.lots;
            takenCost += match.lots * match.ticks;
            lastLots = match.lots;
            lastTicks = match.ticks;

            final Exec makerTrade = factory.newExec(makerOrder, makerId, now);
            makerTrade.trade(match.lots, match.ticks, takerId, Role.MAKER, takerOrder.getTrader());
            match.makerTrade = makerTrade;

            final Exec takerTrade = factory.newExec(takerOrder, takerId, now);
            takerTrade.trade(takenLots, takenCost, match.lots, match.ticks, makerId, Role.TAKER,
                    makerOrder.getTrader());
            match.takerTrade = takerTrade;

            result.matches.insertBack(match);

            // Maker updated first because this is consistent with last-look semantics.
            // N.B. the reference count is not incremented here.
            result.execs.insertBack(makerTrade);
            result.execs.insertBack(takerTrade);
        }

        if (!result.matches.isEmpty()) {
            // Avoid allocating position when there are no matches.
            result.posn = takerSess.getLazyPosn(book);
            takerOrder.trade(takenLots, takenCost, lastLots, lastTicks, now);
        }
    }

    private final void matchOrders(TraderSess sess, MarketBook book, Order order, Result result) {
        BookSide bookSide;
        Direct direct;
        if (order.getSide() == Side.BUY) {
            // Paid when the taker lifts the offer.
            bookSide = book.getOfferSide();
            direct = Direct.PAID;
        } else {
            assert order.getSide() == Side.SELL;
            // Given when the taker hits the bid.
            bookSide = book.getBidSide();
            direct = Direct.GIVEN;
        }
        matchOrders(sess, book, order, bookSide, direct, result);
    }

    private final @Nullable Order matchQuote(MarketBook book, Side side, long lots) {
        final BookSide bookSide = book.getOtherSide(side);
        for (DlNode node = bookSide.getFirstOrder(); !node.isEnd(); node = node.dlNext()) {
            final Order makerOrder = (Order) node;
            if (makerOrder.getAvail() >= lots) {
                return makerOrder;
            }
        }
        return null;
    }

    private final void doCancelOrders(MarketBook book, long now)
            throws NotFoundException, ServiceUnavailableException {
        final BookSide bidSide = book.getBidSide();
        final BookSide offerSide = book.getOfferSide();

        // Build list of cancel executions.

        JslNode firstExec = null;
        for (DlNode node = bidSide.getFirstOrder(); !node.isEnd(); node = node.dlNext()) {
            final Order order = (Order) node;
            final Exec exec = newExec(book, order, now);
            exec.cancel(order.getQuot());
            // Stack push.
            exec.setJslNext(firstExec);
            firstExec = exec;
        }
        for (DlNode node = offerSide.getFirstOrder(); !node.isEnd(); node = node.dlNext()) {
            final Order order = (Order) node;
            final Exec exec = newExec(book, order, now);
            exec.cancel(order.getQuot());
            // Stack push.
            exec.setJslNext(firstExec);
            firstExec = exec;
        }
        if (firstExec == null) {
            return;
        }

        try {
            journ.createExecList(book.getMnem(), firstExec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(book);

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

    // Assumes that maker lots have not been reduced since matching took place.

    private final void commitMatches(TraderSess taker, MarketBook book, long now, Result result) {
        setDirty(book);
        SlNode node = result.matches.getFirst();
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
            final Posn posn = result.posn;
            assert posn != null;
            posn.addTrade(takerTrade);
            setDirty(maker,
                    TraderSess.DIRTY_ORDER | TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);
            // Next match.
            node = node.slNext();
        } while (node != null);
    }

    public Serv(Model model, Journ journ, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this.journ = journ;
        this.cache = cache;
        this.factory = factory;
        this.dirty = DIRTY_ALL;

        RecTree t = model.readAsset(factory);
        assert t != null;
        this.assets = t;

        t = model.readContr(factory);
        assert t != null;
        this.contrs = t;
        enrichContrs();

        t = model.readMarket(factory);
        assert t != null;
        this.markets = t;
        enrichMarkets();

        t = model.readTrader(factory);
        assert t != null;
        this.traders = t;

        final SlNode firstOrder = model.readOrder(factory);
        for (SlNode node = firstOrder; node != null;) {
            final Order order = (Order) node;
            node = popNext(node);

            // This method will mark the book as dirty.
            insertOrder(order);
        }

        final SlNode firstTrade = model.readTrade(factory);
        for (SlNode node = firstTrade; node != null;) {
            final Exec trade = (Exec) node;
            node = popNext(node);

            final TraderSess sess = (TraderSess) traders.find(trade.getTrader());
            assert sess != null;
            sess.insertTrade(trade);
        }

        final SlNode firstPosn = model.readPosn(getBusDate(now).toJd(), factory);
        for (SlNode node = firstPosn; node != null;) {
            final Posn posn = (Posn) node;
            node = popNext(node);

            final TraderSess sess = (TraderSess) traders.find(posn.getTrader());
            assert sess != null;
            sess.insertPosn(posn);
        }

        for (RbNode node = traders.getFirst(); node != null; node = node.rbNext()) {
            final TraderSess sess = (TraderSess) node;
            emailIdx.insert(sess);
            setDirty(sess, TraderSess.DIRTY_ALL);
        }

        updateDirty();
    }

    public Serv(Datastore datastore, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this(datastore, datastore, cache, factory, now);
    }

    public final TraderSess createTrader(String mnem, @Nullable String display, String email)
            throws BadRequestException, ServiceUnavailableException {
        if (traders.find(mnem) != null) {
            throw new AlreadyExistsException(String.format("trader '%s' already exists", mnem));
        }
        if (emailIdx.find(email) != null) {
            throw new AlreadyExistsException(String.format("email '%s' is already in use", email));
        }
        final TraderSess sess = newTrader(mnem, display, email);

        try {
            journ.createTrader(mnem, display, email);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(DIRTY_TRADER);
        setDirty(sess, TraderSess.DIRTY_ALL);

        traders.insert(sess);
        emailIdx.insert(sess);

        updateDirty();
        return sess;
    }

    public final TraderSess updateTrader(String mnem, @Nullable String display)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final TraderSess sess = (TraderSess) traders.find(mnem);
        if (sess == null) {
            throw new TraderNotFoundException(String.format("trader '%s' does not exist", mnem));
        }

        try {
            journ.updateTrader(mnem, display);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(DIRTY_TRADER);

        sess.setDisplay(display);

        updateDirty();
        return sess;
    }

    public final @Nullable Rec findRec(RecType recType, String mnem) {
        Rec rec = null;
        switch (recType) {
        case ASSET:
            rec = assets.find(mnem);
            break;
        case CONTR:
            rec = contrs.find(mnem);
            break;
        case MARKET:
            rec = markets.find(mnem);
            break;
        case TRADER:
            rec = traders.find(mnem);
            break;
        }
        return rec;
    }

    public final @Nullable Rec getRootRec(RecType recType) {
        Rec rec = null;
        switch (recType) {
        case ASSET:
            rec = assets.getRoot();
            break;
        case CONTR:
            rec = contrs.getRoot();
            break;
        case MARKET:
            rec = markets.getRoot();
            break;
        case TRADER:
            rec = traders.getRoot();
            break;
        }
        return rec;
    }

    public final @Nullable Rec getFirstRec(RecType recType) {
        Rec rec = null;
        switch (recType) {
        case ASSET:
            rec = assets.getFirst();
            break;
        case CONTR:
            rec = contrs.getFirst();
            break;
        case MARKET:
            rec = markets.getFirst();
            break;
        case TRADER:
            rec = traders.getFirst();
            break;
        }
        return rec;
    }

    public final @Nullable Rec getLastRec(RecType recType) {
        Rec rec = null;
        switch (recType) {
        case ASSET:
            rec = assets.getLast();
            break;
        case CONTR:
            rec = contrs.getLast();
            break;
        case MARKET:
            rec = markets.getLast();
            break;
        case TRADER:
            rec = traders.getLast();
            break;
        }
        return rec;
    }

    public final boolean isEmptyRec(RecType recType) {
        boolean result = true;
        switch (recType) {
        case ASSET:
            result = assets.isEmpty();
            break;
        case CONTR:
            result = contrs.isEmpty();
            break;
        case MARKET:
            result = markets.isEmpty();
            break;
        case TRADER:
            result = traders.isEmpty();
            break;
        }
        return result;
    }

    public final MarketBook getMarket(String mnem) throws NotFoundException {
        final MarketBook book = (MarketBook) markets.find(mnem);
        if (book == null) {
            throw new MarketNotFoundException(String.format("market '%s' does not exist", mnem));
        }
        return book;
    }

    public final TraderSess getTrader(String mnem) throws NotFoundException {
        final TraderSess sess = (TraderSess) traders.find(mnem);
        if (sess == null) {
            throw new TraderNotFoundException(String.format("trader '%s' does not exist", mnem));
        }
        return sess;
    }

    public final @Nullable TraderSess findTraderByEmail(String email) {
        return (TraderSess) emailIdx.find(email);
    }

    public final MarketBook createMarket(String mnem, @Nullable String display, Contr contr,
            int settlDay, int expiryDay, int state, long now)
                    throws BadRequestException, ServiceUnavailableException {
        if (settlDay != 0) {
            // busDay <= expiryDay <= settlDay.
            final int busDay = getBusDate(now).toJd();
            if (settlDay < expiryDay) {
                throw new InvalidException("settl-day before expiry-day");
            }
            if (expiryDay < busDay) {
                throw new InvalidException("expiry-day before bus-day");
            }
        } else {
            if (expiryDay != 0) {
                throw new InvalidException("expiry-day without settl-day");
            }
        }
        MarketBook book = (MarketBook) markets.pfind(mnem);
        if (book != null && book.getMnem().equals(mnem)) {
            throw new AlreadyExistsException(String.format("market '%s' already exists", mnem));
        }
        final MarketBook parent = book;
        book = newMarket(mnem, display, contr, settlDay, expiryDay, state);

        try {
            journ.createMarket(mnem, display, contr.getMnem(), settlDay, expiryDay, state);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(DIRTY_MARKET | DIRTY_VIEW);

        markets.pinsert(book, parent);
        views.insert(book.getView());

        updateDirty();
        return book;
    }

    public final MarketBook createMarket(String mnem, @Nullable String display, String contrMnem,
            int settlDay, int expiryDay, int state, long now)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Contr contr = (Contr) contrs.find(contrMnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contr '%s' does not exist", contrMnem));
        }
        return createMarket(mnem, display, contr, settlDay, expiryDay, state, now);
    }

    public final MarketBook updateMarket(String mnem, @Nullable String display, int state, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final MarketBook book = (MarketBook) markets.find(mnem);
        if (book == null) {
            throw new MarketNotFoundException(String.format("market '%s' does not exist", mnem));
        }

        try {
            journ.updateMarket(mnem, display, state);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(DIRTY_MARKET);

        book.setDisplay(display);
        book.setState(state);

        updateDirty();
        return book;
    }

    public final void createOrder(TraderSess sess, MarketBook book, @Nullable String ref, Side side,
            long lots, long ticks, long minLots, long now, Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final int busDay = getBusDate(now).toJd();
        if (book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
            throw new MarketClosedException(String.format("market for '%s' on '%d' has expired",
                    book.getContrRich().getMnem(), maybeJdToIso(book.getSettlDay())));
        }
        if (lots == 0 || lots < minLots) {
            throw new InvalidLotsException(String.format("invalid lots '%d'", lots));
        }
        final long orderId = book.allocOrderId();
        final Order order = factory.newOrder(sess.getMnem(), book, orderId, ref, side, lots, ticks,
                minLots, now);

        final Exec exec = newExec(book, order, now);
        result.reset(sess.getMnem(), book, order, exec);
        // Order fields are updated on match.
        matchOrders(sess, book, order, result);
        // Place incomplete order in market.
        if (!order.isDone()) {
            // This may fail if level cannot be allocated.
            book.insertOrder(order);
        }
        // TODO: IOC orders would need an additional revision for the unsolicited cancellation of
        // any unfilled quantity.
        boolean success = false;
        try {
            final JslNode firstExec = result.prepareExecList();
            assert firstExec != null;
            journ.createExecList(book.getMnem(), firstExec);
            success = true;
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        } finally {
            if (!success && !order.isDone()) {
                // Undo market insertion.
                book.removeOrder(order);
            }
        }

        // Commit phase.

        sess.insertOrder(order);
        // Commit trans to cycle and free matches.
        commitMatches(sess, book, now, result);

        updateDirty();
    }

    public final void reviseOrder(TraderSess sess, MarketBook book, Order order, long lots,
            long now, Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (order.isDone()) {
            throw new TooLateException(String.format("order '%d' is done", order.getId()));
        }
        // Revised lots must not be:
        // 1. less than min lots;
        // 2. less than executed lots;
        // 3. greater than original lots.
        if (lots == 0 || lots < order.getMinLots() || lots < order.getExec()
                || lots > order.getLots()) {
            throw new InvalidLotsException(String.format("invalid lots '%d'", lots));
        }

        final Exec exec = newExec(book, order, now);
        exec.revise(lots);
        result.reset(sess.getMnem(), book, order, exec);
        try {
            journ.createExec(exec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(book);
        setDirty(sess, TraderSess.DIRTY_ORDER);

        book.reviseOrder(order, lots, now);

        updateDirty();
    }

    public final void reviseOrder(TraderSess sess, MarketBook book, long id, long lots, long now,
            Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(book.getMnem(), id);
        if (order == null) {
            throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
        }
        reviseOrder(sess, book, order, lots, now, result);
    }

    public final void reviseOrder(TraderSess sess, MarketBook book, String ref, long lots, long now,
            Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(ref);
        if (order == null) {
            throw new OrderNotFoundException(String.format("order '%s' does not exist", ref));
        }
        reviseOrder(sess, book, order, lots, now, result);
    }

    public final void reviseOrder(TraderSess sess, MarketBook book, JslNode first, long lots,
            long now, Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {

        result.reset(sess.getMnem(), book);

        final String market = book.getMarket();
        JslNode jslNode = first;
        do {
            final MarketId mid = (MarketId) jslNode;
            jslNode = jslNode.jslNext();

            final long id = mid.getId();

            final Order order = sess.findOrder(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            if (order.isDone()) {
                throw new TooLateException(String.format("order '%d' is done", id));
            }
            // Revised lots must not be:
            // 1. less than min lots;
            // 2. less than executed lots;
            // 3. greater than original lots.
            if (lots == 0 || lots < order.getMinLots() || lots < order.getExec()
                    || lots > order.getLots()) {
                throw new InvalidLotsException(String.format("invalid lots '%d'", lots));
            }

            final Exec exec = newExec(book, order, now);
            exec.revise(lots);

            result.orders.insertBack(order);
            result.execs.insertBack(exec);

        } while (jslNode != null);

        try {
            final JslNode firstExec = result.prepareExecList();
            assert firstExec != null;
            journ.createExecList(book.getMnem(), firstExec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(book);
        setDirty(sess, TraderSess.DIRTY_ORDER);

        SlNode node = result.getFirstExec();
        assert node != null;
        do {
            final Exec exec = (Exec) node;
            node = node.slNext();

            final Order order = sess.findOrder(market, exec.getOrderId());
            assert order != null;

            book.reviseOrder(order, lots, now);

        } while (node != null);

        updateDirty();
    }

    public final void cancelOrder(TraderSess sess, MarketBook book, Order order, long now,
            Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (order.isDone()) {
            throw new TooLateException(String.format("order '%d' is done", order.getId()));
        }

        final Exec exec = newExec(book, order, now);
        exec.cancel(order.getQuot());
        result.reset(sess.getMnem(), book, order, exec);
        try {
            journ.createExec(exec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(book);
        setDirty(sess, TraderSess.DIRTY_ORDER);

        book.cancelOrder(order, now);

        updateDirty();
    }

    public final void cancelOrder(TraderSess sess, MarketBook book, long id, long now,
            Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(book.getMnem(), id);
        if (order == null) {
            throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
        }
        cancelOrder(sess, book, order, now, result);
    }

    public final void cancelOrder(TraderSess sess, MarketBook book, String ref, long now,
            Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(ref);
        if (order == null) {
            throw new OrderNotFoundException(String.format("order '%s' does not exist", ref));
        }
        cancelOrder(sess, book, order, now, result);
    }

    public final void cancelOrder(TraderSess sess, MarketBook book, JslNode first, long now,
            Result result)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {

        result.reset(sess.getMnem(), book);

        final String market = book.getMarket();
        JslNode jslNode = first;
        do {
            final MarketId mid = (MarketId) jslNode;
            jslNode = jslNode.jslNext();

            final long id = mid.getId();
            final Order order = sess.findOrder(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            if (order.isDone()) {
                throw new TooLateException(String.format("order '%d' is done", id));
            }

            final Exec exec = newExec(book, order, now);
            exec.cancel(order.getQuot());

            result.orders.insertBack(order);
            result.execs.insertBack(exec);

        } while (jslNode != null);

        try {
            final JslNode firstExec = result.prepareExecList();
            assert firstExec != null;
            journ.createExecList(book.getMnem(), firstExec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(book);
        setDirty(sess, TraderSess.DIRTY_ORDER);

        SlNode node = result.getFirstExec();
        assert node != null;
        do {
            final Exec exec = (Exec) node;
            node = node.slNext();

            final Order order = sess.findOrder(market, exec.getOrderId());
            assert order != null;

            book.cancelOrder(order, now);

        } while (node != null);

        updateDirty();
    }

    /**
     * Cancels all orders.
     * 
     * @param sess
     *            The session.
     * @param now
     *            The current time.
     * @throws NotFoundException
     * @throws ServiceUnavailableException
     */
    public final void cancelOrder(TraderSess sess, long now)
            throws NotFoundException, ServiceUnavailableException {

        // Build list of cancel executions.

        JslNode firstExec = null;
        for (RbNode node = sess.getFirstOrder(); node != null; node = node.rbNext()) {
            final Order order = sess.getRootOrder();
            assert order != null;
            if (!order.isDone()) {
                continue;
            }
            final MarketBook book = (MarketBook) markets.find(order.getMarket());
            assert book != null;

            final Exec exec = newExec(book, order, now);
            exec.cancel(order.getQuot());
            // Stack push.
            exec.setJslNext(firstExec);
            firstExec = exec;
        }
        if (firstExec == null) {
            return;
        }

        try {
            journ.createExecList(firstExec);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(sess, TraderSess.DIRTY_ORDER);

        for (;;) {
            final Order order = sess.getRootOrder();
            if (order == null) {
                break;
            }
            if (!order.isDone()) {
                continue;
            }
            final MarketBook book = (MarketBook) markets.find(order.getMarket());
            assert book != null;

            setDirty(book);
            book.cancelOrder(order, now);
        }

        updateDirty();
    }

    public final void cancelOrder(MarketBook book, long now)
            throws NotFoundException, ServiceUnavailableException {
        doCancelOrders(book, now);
        updateDirty();
    }

    public final void archiveOrder(TraderSess sess, Order order, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (!order.isDone()) {
            throw new InvalidException(String.format("order '%d' is not done", order.getId()));
        }
        try {
            journ.archiveOrder(order.getMarket(), order.getId(), now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(sess, TraderSess.DIRTY_ORDER);

        // No need to update timestamps on order because it is immediately freed.
        sess.removeOrder(order);

        updateDirty();
    }

    public final void archiveOrder(TraderSess sess, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(market, id);
        if (order == null) {
            throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
        }
        archiveOrder(sess, order, now);
    }

    /**
     * Archive all orders.
     * 
     * @param sess
     *            The session.
     * @param now
     *            The current time.
     * @throws NotFoundException
     * @throws ServiceUnavailableException
     */
    public final void archiveOrder(TraderSess sess, long now)
            throws NotFoundException, ServiceUnavailableException {

        MarketId firstMid = null;
        for (RbNode node = sess.getFirstOrder(); node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (!order.isDone()) {
                continue;
            }
            final MarketId mid = new MarketId(order.getMarket(), order.getId());
            mid.setJslNext(firstMid);
            firstMid = mid;
        }
        if (firstMid == null) {
            return;
        }

        try {
            journ.archiveOrderList(firstMid, now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(sess, TraderSess.DIRTY_ORDER);

        RbNode node = sess.getFirstOrder();
        while (node != null) {
            final Order order = (Order) node;
            // Move to next node before this order is unlinked.
            node = node.rbNext();
            if (!order.isDone()) {
                continue;
            }
            // No need to update timestamps on order because it is immediately freed.
            sess.removeOrder(order);
        }

        updateDirty();
    }

    public final void archiveOrder(TraderSess sess, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {

        JslNode node = first;
        do {
            final MarketId mid = (MarketId) node;
            node = node.jslNext();

            final long id = mid.getId();
            final Order order = sess.findOrder(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            if (!order.isDone()) {
                throw new InvalidException(String.format("order '%d' is not done", order.getId()));
            }
        } while (node != null);

        try {
            journ.archiveOrderList(market, first, now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(sess, TraderSess.DIRTY_ORDER);

        // The list can be safely traversed here because the archive operation will not modify it.
        node = first;
        do {
            final MarketId mid = (MarketId) node;
            node = node.jslNext();

            final long id = mid.getId();
            final Order order = sess.findOrder(market, id);
            assert order != null;
            // No need to update timestamps on order because it is immediately freed.
            sess.removeOrder(order);
        } while (node != null);

        updateDirty();
    }

    public final Exec createTrade(TraderSess sess, MarketBook book, String ref, Side side,
            long lots, long ticks, @Nullable Role role, @Nullable String cpty, long created)
                    throws NotFoundException, ServiceUnavailableException {
        final Posn posn = sess.getLazyPosn(book);
        final Exec trade = Exec.manual(sess.getMnem(), book.getMnem(), book.getContr(),
                book.getSettlDay(), book.allocExecId(), ref, side, lots, ticks, role, cpty,
                created);
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
                journ.createExecList(book.getMnem(), trade);
            } catch (final RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // Commit phase.

            // Update counter-party cache entries.
            setDirty(cptySess, TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);

            sess.insertTrade(trade);
            posn.addTrade(trade);
            cptySess.insertTrade(cptyTrade);
            cptyPosn.addTrade(cptyTrade);

        } else {

            try {
                journ.createExec(trade);
            } catch (final RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // Commit phase.

            sess.insertTrade(trade);
            posn.addTrade(trade);
        }
        setDirty(sess, TraderSess.DIRTY_TRADE | TraderSess.DIRTY_POSN);
        updateDirty();
        return trade;
    }

    public final void archiveTrade(TraderSess sess, Exec trade, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (trade.getState() != State.TRADE) {
            throw new InvalidException(String.format("exec '%d' is not a trade", trade.getId()));
        }
        try {
            journ.archiveTrade(trade.getMarket(), trade.getId(), now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(sess, TraderSess.DIRTY_TRADE);

        // No need to update timestamps on trade because it is immediately freed.
        sess.removeTrade(trade);

        updateDirty();
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
     * @param sess
     *            The session.
     * @param now
     *            The current time.
     * @throws NotFoundException
     * @throws ServiceUnavailableException
     */
    public final void archiveTrade(TraderSess sess, long now)
            throws NotFoundException, ServiceUnavailableException {

        MarketId firstMid = null;
        for (RbNode node = sess.getFirstTrade(); node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            final MarketId mid = new MarketId(trade.getMarket(), trade.getId());
            mid.setJslNext(firstMid);
            firstMid = mid;
        }
        if (firstMid == null) {
            return;
        }

        try {
            journ.archiveTradeList(firstMid, now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(sess, TraderSess.DIRTY_TRADE);

        for (;;) {
            final Exec trade = sess.getRootTrade();
            if (trade == null) {
                break;
            }
            // No need to update timestamps on trade because it is immediately freed.
            sess.removeTrade(trade);
        }

        updateDirty();
    }

    public final void archiveTrade(TraderSess sess, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {

        JslNode node = first;
        do {
            final MarketId mid = (MarketId) node;
            node = node.jslNext();

            final long id = mid.getId();
            final Exec trade = sess.findTrade(market, id);
            if (trade == null) {
                throw new NotFoundException(String.format("trade '%d' does not exist", id));
            }
        } while (node != null);

        try {
            journ.archiveTradeList(market, first, now);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(sess, TraderSess.DIRTY_TRADE);

        // The list can be safely traversed here because the archive operation will not modify it.
        node = first;
        do {
            final MarketId mid = (MarketId) node;
            node = node.jslNext();

            final long id = mid.getId();
            final Exec trade = sess.findTrade(market, id);
            assert trade != null;
            // No need to update timestamps on order because it is immediately freed.
            sess.removeTrade(trade);
        } while (node != null);

        updateDirty();
    }

    public final Quote createQuote(TraderSess sess, MarketBook book, @Nullable String ref,
            Side side, long lots, long now) throws NotFoundException, ServiceUnavailableException {
        final Order makerOrder = matchQuote(book, side, lots);
        if (makerOrder == null) {
            throw new NotFoundException("no liquidity available");
        }

        final Quote quote = factory.newQuote(sess.getMnem(), book, book.allocQuoteId(), ref, side,
                lots, makerOrder.getTicks(), now, now + QUOTE_EXPIRY);

        try {
            journ.createQuote(quote);
        } catch (final RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Commit phase.

        setDirty(DIRTY_TIMEOUT);
        setDirty(book);
        setDirty(sess, TraderSess.DIRTY_QUOTE);

        quotes.add(quote);
        sess.insertQuote(quote);

        updateDirty();
        return quote;
    }

    /**
     * This method may partially fail.
     * 
     * @param now
     *            The current time.
     * @throws NotFoundException
     * @throws ServiceUnavailableException
     */
    public final void expireEndOfDay(long now)
            throws NotFoundException, ServiceUnavailableException {
        final int busDay = getBusDate(now).toJd();
        for (RbNode node = markets.getFirst(); node != null;) {
            final MarketBook book = (MarketBook) node;
            node = node.rbNext();
            if (book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
                doCancelOrders(book, now);
            }
        }
        updateDirty();
    }

    public final void settlEndOfDay(long now) {
        final int busDay = getBusDate(now).toJd();
        for (RbNode node = markets.getFirst(); node != null;) {
            final MarketBook book = (MarketBook) node;
            node = node.rbNext();
            if (book.isSettlDaySet() && book.getSettlDay() <= busDay) {
                views.remove(book.getView());
                markets.remove(book);
                setDirty(DIRTY_MARKET | DIRTY_VIEW);
            }
        }
        for (RbNode node = traders.getFirst(); node != null; node = node.rbNext()) {
            final TraderSess sess = (TraderSess) node;
            if (sess.settlPosns(busDay) > 0) {
                setDirty(sess, TraderSess.DIRTY_POSN);
            }
        }
        updateDirty();
    }

    public final void poll(long now) {
        for (;;) {
            final Quote quote = quotes.getFirst();
            // Break if no quote or not expired.
            if (quote == null || quote.getExpiry() > now) {
                break;
            }

            final TraderSess sess = (TraderSess) traders.find(quote.getTrader());
            assert sess != null;

            final MarketBook book = (MarketBook) markets.find(quote.getMarket());
            assert book != null;

            setDirty(DIRTY_TIMEOUT);
            setDirty(book);
            setDirty(sess, TraderSess.DIRTY_QUOTE);

            quotes.removeFirst();
            sess.removeQuote(quote);
        }
        updateDirty();
    }

    public final long getTimeout() {
        final Quote next = quotes.getFirst();
        return next != null ? next.getExpiry() : 0;
    }
}

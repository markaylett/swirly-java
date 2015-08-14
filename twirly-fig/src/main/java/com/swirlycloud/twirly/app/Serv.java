/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeJdToIso;
import static com.swirlycloud.twirly.node.SlUtil.popNext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.date.DateUtil;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Direct;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Instruct;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.intrusive.EmailHashTable;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.io.AsyncDatastore;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.Journ;
import com.swirlycloud.twirly.node.DlNode;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;

public @NonNullByDefault class Serv implements AutoCloseable {

    private static final int CAPACITY = 1 << 5; // 64
    @SuppressWarnings("null")
    private static final Pattern MNEM_PATTERN = Pattern.compile("^[0-9A-Za-z-._]{3,16}$");

    private final Journ journ;
    private final Factory factory;
    private final MnemRbTree assets;
    private final MnemRbTree contrs;
    private final MnemRbTree markets;
    private final MnemRbTree traders;
    private final EmailHashTable emailIdx = new EmailHashTable(CAPACITY);

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
        final Trader trader = (Trader) traders.find(order.getTrader());
        assert trader != null;
        final TraderSess sess = getLazySess(trader);
        sess.insertOrder(order);
        if (!order.isDone()) {
            final Market market = (Market) markets.find(order.getMarket());
            boolean success = false;
            try {
                assert market != null;
                market.insertOrder(order);
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
            final Market market = (Market) node;
            enrichMarket(market);
        }
    }

    private final void updateEmailIdx() {
        for (RbNode node = traders.getFirst(); node != null; node = node.rbNext()) {
            final Trader trader = (Trader) node;
            emailIdx.insert(trader);
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

            final Trader trader = (Trader) traders.find(trade.getTrader());
            assert trader != null;
            final TraderSess sess = getLazySess(trader);
            sess.insertTrade(trade);
        }
    }

    private final void insertPosns(@Nullable SlNode first) {
        for (SlNode node = first; node != null;) {
            final Posn posn = (Posn) node;
            node = popNext(node);

            final Trader trader = (Trader) traders.find(posn.getTrader());
            assert trader != null;
            final TraderSess sess = getLazySess(trader);
            sess.insertPosn(posn);
        }
    }

    private final Trader newTrader(String mnem, String display, String email)
            throws BadRequestException {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new BadRequestException(String.format("invalid mnem '%s'", mnem));
        }
        return factory.newTrader(mnem, display, email);
    }

    private final Market newMarket(String mnem, String display, Contr contr, int settlDay,
            int expiryDay, int state) throws BadRequestException {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new BadRequestException(String.format("invalid mnem '%s'", mnem));
        }
        return factory.newMarket(mnem, display, contr, settlDay, expiryDay, state);
    }

    private final Exec newExec(Market market, Instruct instruct, long now) {
        return factory.newExec(market.allocExecId(), instruct, now);
    }

    private static long spread(Order takerOrder, Order makerOrder, Direct direct) {
        return direct == Direct.PAID
        // Paid when the taker lifts the offer.
        ? makerOrder.getTicks() - takerOrder.getTicks()
                // Given when the taker hits the bid.
                : takerOrder.getTicks() - makerOrder.getTicks();
    }

    private final void matchOrders(TraderSess takerSess, Market market, Order takerOrder,
            Side side, Direct direct, Trans trans) {

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

            final long makerId = market.allocExecId();
            final long takerId = market.allocExecId();

            final TraderSess makerSess = (TraderSess) traders.find(makerOrder.getTrader());
            assert makerSess != null;
            final Posn makerPosn = makerSess.getLazyPosn(market);

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
            trans.posn = takerSess.getLazyPosn(market);
            takerOrder.trade(takenLots, takenCost, lastTicks, lastLots, now);
        }
    }

    private final void matchOrders(TraderSess sess, Market market, Order order, Trans trans) {
        Side side;
        Direct direct;
        if (order.getAction() == Action.BUY) {
            // Paid when the taker lifts the offer.
            side = market.getOfferSide();
            direct = Direct.PAID;
        } else {
            assert order.getAction() == Action.SELL;
            // Given when the taker hits the bid.
            side = market.getBidSide();
            direct = Direct.GIVEN;
        }
        matchOrders(sess, market, order, side, direct, trans);
    }

    // Assumes that maker lots have not been reduced since matching took place.

    private final void commitMatches(TraderSess taker, Market market, long now, Trans trans) {
        for (SlNode node = trans.matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            final Order makerOrder = match.getMakerOrder();
            assert makerOrder != null;
            // Reduce maker.
            market.takeOrder(makerOrder, match.getLots(), now);
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
        }
    }

    /**
     * Ownership and responsibility for closing the datastore will transferred to the new instance.
     * 
     * @param datastore
     *            The datastore.
     * @param factory
     *            The factory.
     * @param now
     *            The current time.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Serv(AsyncDatastore datastore, Factory factory, long now) throws InterruptedException,
            ExecutionException {
        this.journ = datastore;
        this.factory = factory;
        final int busDay = DateUtil.getBusDate(now).toJd();
        final Future<MnemRbTree> assets = datastore.selectAsset();
        final Future<MnemRbTree> contrs = datastore.selectContr();
        final Future<MnemRbTree> markets = datastore.selectMarket();
        final Future<MnemRbTree> traders = datastore.selectTrader();
        final Future<SlNode> orders = datastore.selectOrder();
        final Future<SlNode> trades = datastore.selectTrade();
        final Future<SlNode> posns = datastore.selectPosn(busDay);

        MnemRbTree t = assets.get();
        assert t != null;
        this.assets = t;

        t = contrs.get();
        assert t != null;
        this.contrs = t;
        enrichContrs();

        t = markets.get();
        assert t != null;
        this.markets = t;
        enrichMarkets();

        t = traders.get();
        assert t != null;
        this.traders = t;
        updateEmailIdx();

        insertOrders(orders.get());
        insertTrades(trades.get());
        insertPosns(posns.get());
    }

    /**
     * Ownership and responsibility for closing the datastore will transferred to the new instance.
     * 
     * @param datastore
     *            The datastore.
     * @param factory
     *            The factory.
     * @param now
     *            The current time.
     */
    public Serv(Datastore datastore, Factory factory, long now) {
        this.journ = datastore;
        this.factory = factory;
        final int busDay = DateUtil.getBusDate(now).toJd();

        MnemRbTree t = datastore.selectAsset();
        assert t != null;
        this.assets = t;

        t = datastore.selectContr();
        assert t != null;
        this.contrs = t;
        enrichContrs();

        t = datastore.selectMarket();
        assert t != null;
        this.markets = t;
        enrichMarkets();

        t = datastore.selectTrader();
        assert t != null;
        this.traders = t;
        updateEmailIdx();

        insertOrders(datastore.selectOrder());
        insertTrades(datastore.selectTrade());
        insertPosns(datastore.selectPosn(busDay));
    }

    @Override
    public final void close() throws Exception {
        journ.close();
    }

    public final Trader createTrader(String mnem, String display, String email)
            throws BadRequestException, ServiceUnavailableException {
        if (traders.find(mnem) != null) {
            throw new BadRequestException(String.format("trader '%s' already exists", mnem));
        }
        if (emailIdx.find(email) != null) {
            throw new BadRequestException(String.format("email '%s' is already in use", email));
        }
        final Trader trader = newTrader(mnem, display, email);
        try {
            journ.insertTrader(mnem, display, email);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }
        traders.insert(trader);
        emailIdx.insert(trader);
        return trader;
    }

    public final Trader updateTrader(String mnem, String display) throws BadRequestException,
            NotFoundException, ServiceUnavailableException {
        final Trader trader = (Trader) traders.find(mnem);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", mnem));
        }
        trader.setDisplay(display);
        try {
            journ.updateTrader(mnem, display);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }
        return trader;
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

    public final Market getMarket(String mnem) throws NotFoundException {
        final Market market = (Market) markets.find(mnem);
        if (market == null) {
            throw new NotFoundException(String.format("market '%s' does not exist", mnem));
        }
        return market;
    }

    public final TraderSess getTrader(String mnem) throws NotFoundException {
        final TraderSess sess = (TraderSess) traders.find(mnem);
        if (sess == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", mnem));
        }
        return sess;
    }

    public final TraderSess getTraderByEmail(String email) throws NotFoundException {
        final TraderSess sess = (TraderSess) emailIdx.find(email);
        if (sess == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        return sess;
    }

    public final Market createMarket(String mnem, String display, Contr contr, int settlDay,
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
        Market market = (Market) markets.pfind(mnem);
        if (market != null && market.getMnem().equals(mnem)) {
            throw new BadRequestException(String.format("market '%s' already exists", mnem));
        }
        final RbNode parent = market;
        market = newMarket(mnem, display, contr, settlDay, expiryDay, state);
        try {
            journ.insertMarket(mnem, display, contr.getMnem(), settlDay, expiryDay, state);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        markets.pinsert(market, parent);
        return market;
    }

    public final Market createMarket(String mnem, String display, String contrMnem, int settlDay,
            int expiryDay, int state, long now) throws BadRequestException, NotFoundException,
            ServiceUnavailableException {
        final Contr contr = (Contr) contrs.find(contrMnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contr '%s' does not exist", contrMnem));
        }
        return createMarket(mnem, display, contr, settlDay, expiryDay, state, now);
    }

    public final Market updateMarket(String mnem, String display, int state, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Market market = (Market) markets.find(mnem);
        if (market == null) {
            throw new NotFoundException(String.format("market '%s' does not exist", mnem));
        }
        market.setDisplay(display);
        market.setState(state);
        try {
            journ.updateMarket(mnem, display, state);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }
        return market;
    }

    public final void expireMarkets(long now) throws NotFoundException, ServiceUnavailableException {
        final int busDay = DateUtil.getBusDate(now).toJd();
        for (RbNode node = markets.getFirst(); node != null;) {
            final Market market = (Market) node;
            node = node.rbNext();
            if (market.isExpiryDaySet() && market.getExpiryDay() < busDay) {
                cancelOrders(market, now);
            }
        }
    }

    public final void settlMarkets(long now) {
        final int busDay = DateUtil.getBusDate(now).toJd();
        for (RbNode node = markets.getFirst(); node != null;) {
            final Market market = (Market) node;
            node = node.rbNext();
            if (market.isSettlDaySet() && market.getSettlDay() <= busDay) {
                markets.remove(market);
            }
        }
        for (RbNode node = traders.getFirst(); node != null; node = node.rbNext()) {
            final TraderSess sess = (TraderSess) node;
            sess.settlPosns(busDay);
        }
    }

    public final TraderSess getLazySess(Trader trader) {
        return (TraderSess) trader;
    }

    public final void placeOrder(TraderSess sess, Market market, @Nullable String ref,
            Action action, long ticks, long lots, long minLots, long now, Trans trans)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final int busDay = DateUtil.getBusDate(now).toJd();
        if (market.isExpiryDaySet() && market.getExpiryDay() < busDay) {
            throw new NotFoundException(String.format("market for '%s' on '%d' has expired", market
                    .getContrRich().getMnem(), maybeJdToIso(market.getSettlDay())));
        }
        if (lots == 0 || lots < minLots) {
            throw new BadRequestException(String.format("invalid lots '%d'", lots));
        }
        final long orderId = market.allocOrderId();
        final Order order = factory.newOrder(orderId, sess.getMnem(), market, ref, action, ticks,
                lots, minLots, now);
        final Exec exec = newExec(market, order, now);

        trans.reset(market, order, exec);
        // Order fields are updated on match.
        matchOrders(sess, market, order, trans);
        // Place incomplete order in market.
        if (!order.isDone()) {
            // This may fail if level cannot be allocated.
            market.insertOrder(order);
        }
        // TODO: IOC orders would need an additional revision for the unsolicited cancellation of
        // any unfilled quantity.
        boolean success = false;
        try {
            final SlNode first = trans.prepareExecList();
            journ.insertExecList(market.getMnem(), first);
            success = true;
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        } finally {
            if (!success && !order.isDone()) {
                // Undo market insertion.
                market.removeOrder(order);
            }
        }
        // Final commit phase cannot fail.
        sess.insertOrder(order);
        // Commit trans to cycle and free matches.
        commitMatches(sess, market, now, trans);
    }

    public final void reviseOrder(TraderSess sess, Market market, Order order, long lots, long now,
            Trans trans) throws BadRequestException, NotFoundException, ServiceUnavailableException {
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

        final Exec exec = newExec(market, order, now);
        exec.revise(lots);
        try {
            journ.insertExec(exec);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Final commit phase cannot fail.
        market.reviseOrder(order, lots, now);

        trans.reset(market, order, exec);
    }

    public final void reviseOrder(TraderSess sess, Market market, long id, long lots, long now,
            Trans trans) throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(market.getMnem(), id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        reviseOrder(sess, market, order, lots, now, trans);
    }

    public final void reviseOrder(TraderSess sess, Market market, String ref, long lots, long now,
            Trans trans) throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(ref);
        if (order == null) {
            throw new NotFoundException(String.format("order '%s' does not exist", ref));
        }
        reviseOrder(sess, market, order, lots, now, trans);
    }

    public final void cancelOrder(TraderSess sess, Market market, Order order, long now, Trans trans)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (order.isDone()) {
            throw new BadRequestException(String.format("order '%d' is done", order.getId()));
        }
        final Exec exec = newExec(market, order, now);
        exec.cancel();
        try {
            journ.insertExec(exec);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // Final commit phase cannot fail.
        market.cancelOrder(order, now);

        trans.reset(market, order, exec);
    }

    public final void cancelOrder(TraderSess sess, Market market, long id, long now, Trans trans)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(market.getMnem(), id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        cancelOrder(sess, market, order, now, trans);
    }

    public final void cancelOrder(TraderSess sess, Market market, String ref, long now, Trans trans)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final Order order = sess.findOrder(ref);
        if (order == null) {
            throw new NotFoundException(String.format("order '%s' does not exist", ref));
        }
        cancelOrder(sess, market, order, now, trans);
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
            final Market market = (Market) markets.find(order.getMarket());
            assert market != null;

            final Exec exec = newExec(market, order, now);
            exec.cancel();
            try {
                journ.insertExec(exec);
            } catch (RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // Final commit phase cannot fail.
            market.cancelOrder(order, now);
        }
    }

    public final void cancelOrders(Market market, long now) throws NotFoundException,
            ServiceUnavailableException {
        final Side bidSide = market.getBidSide();
        final Side offerSide = market.getOfferSide();

        SlNode first = null;
        for (DlNode node = bidSide.getFirstOrder(); !node.isEnd(); node = node.dlNext()) {
            final Order order = (Order) node;
            final Exec exec = newExec(market, order, now);
            exec.cancel();
            // Stack push.
            exec.setSlNext(first);
            first = exec;
        }
        for (DlNode node = offerSide.getFirstOrder(); !node.isEnd(); node = node.dlNext()) {
            final Order order = (Order) node;
            final Exec exec = newExec(market, order, now);
            exec.cancel();
            // Stack push.
            exec.setSlNext(first);
            first = exec;
        }
        try {
            journ.insertExecList(market.getMnem(), first);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }
        // Commit phase.
        for (DlNode node = bidSide.getFirstOrder(); !node.isEnd();) {
            final Order order = (Order) node;
            node = node.dlNext();
            bidSide.cancelOrder(order, now);
        }
        for (DlNode node = offerSide.getFirstOrder(); !node.isEnd();) {
            final Order order = (Order) node;
            node = node.dlNext();
            offerSide.cancelOrder(order, now);
        }
    }

    public final void archiveOrder(TraderSess sess, Order order, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (!order.isDone()) {
            throw new BadRequestException(String.format("order '%d' is not done", order.getId()));
        }
        try {
            journ.archiveOrder(order.getMarket(), order.getId(), now);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // No need to update timestamps on order because it is immediately freed.
        sess.removeOrder(order);
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
            } catch (RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // No need to update timestamps on order because it is immediately freed.
            sess.removeOrder(order);
        }
    }

    public final Exec createTrade(TraderSess sess, Market market, String ref, Action action,
            long ticks, long lots, @Nullable Role role, @Nullable String cpty, long created)
            throws NotFoundException, ServiceUnavailableException {
        final Posn posn = sess.getLazyPosn(market);
        final Exec trade = Exec.manual(market.allocExecId(), sess.getMnem(), market.getMnem(),
                market.getContr(), market.getSettlDay(), ref, action, ticks, lots, role, cpty,
                created);
        if (cpty != null) {
            // Create back-to-back trade if counter-party is specified.
            final TraderSess cptySess = (TraderSess) traders.find(cpty);
            if (cptySess == null) {
                throw new NotFoundException(String.format("cpty '%s' does not exist", cpty));
            }
            final Posn cptyPosn = cptySess.getLazyPosn(market);
            final Exec cptyTrade = trade.inverse(market.allocExecId());
            trade.setSlNext(cptyTrade);
            try {
                journ.insertExecList(market.getMnem(), trade);
            } catch (RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }
            sess.insertTrade(trade);
            posn.addTrade(trade);
            cptySess.insertTrade(cptyTrade);
            cptyPosn.addTrade(cptyTrade);
        } else {
            try {
                journ.insertExec(trade);
            } catch (RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }
            sess.insertTrade(trade);
            posn.addTrade(trade);
        }
        return trade;
    }

    public final void archiveTrade(TraderSess sess, Exec trade, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        if (trade.getState() != State.TRADE) {
            throw new BadRequestException(String.format("exec '%d' is not a trade", trade.getId()));
        }
        try {
            journ.archiveTrade(trade.getMarket(), trade.getId(), now);
        } catch (RejectedExecutionException e) {
            throw new ServiceUnavailableException("journal is busy", e);
        }

        // No need to update timestamps on trade because it is immediately freed.
        sess.removeTrade(trade);
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
        for (;;) {
            final Exec trade = (Exec) sess.getRootTrade();
            if (trade == null) {
                break;
            }
            try {
                journ.archiveTrade(trade.getMarket(), trade.getId(), now);
            } catch (RejectedExecutionException e) {
                throw new ServiceUnavailableException("journal is busy", e);
            }

            // No need to update timestamps on trade because it is immediately freed.
            sess.removeTrade(trade);
        }
    }

    public final void archiveAll(TraderSess sess, long now) throws NotFoundException,
            ServiceUnavailableException {
        archiveOrders(sess, now);
        archiveTrades(sess, now);
    }
}

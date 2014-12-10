/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static com.swirlycloud.util.Date.jdToIso;

import java.util.regex.Pattern;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Direct;
import com.swirlycloud.domain.EmailIdx;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Instruct;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.domain.RecType;
import com.swirlycloud.domain.RefIdx;
import com.swirlycloud.domain.Role;
import com.swirlycloud.domain.Side;
import com.swirlycloud.domain.State;
import com.swirlycloud.domain.Trader;
import com.swirlycloud.function.UnaryCallback;
import com.swirlycloud.util.DlNode;
import com.swirlycloud.util.RbNode;
import com.swirlycloud.util.SlNode;
import com.swirlycloud.util.Tree;

public final class Serv implements AutoCloseable {
    private static final int BUCKETS = 257;
    private static final Pattern MNEM_PATTERN = Pattern.compile("^[0-9A-Za-z_]{3,16}$");

    private final Model model;
    private final Cache cache = new Cache(BUCKETS);
    private final EmailIdx emailIdx = new EmailIdx(BUCKETS);
    private final RefIdx refIdx = new RefIdx(BUCKETS);
    private final Tree markets = new Tree();
    private final Tree accnts = new Tree();

    private final void enrichMarket(Market market) {
        final Contr contr = (Contr) cache.findRec(RecType.CONTR, market.getContrId());
        market.enrich(contr);
    }

    private final void enrichOrder(Order order) {
        final Trader trader = (Trader) cache.findRec(RecType.TRADER, order.getTraderId());
        final Contr contr = (Contr) cache.findRec(RecType.CONTR, order.getContrId());
        order.enrich(trader, contr);
    }

    private final void enrichTrade(Exec trade) {
        final Trader trader = (Trader) cache.findRec(RecType.TRADER, trade.getTraderId());
        final Contr contr = (Contr) cache.findRec(RecType.CONTR, trade.getContrId());
        final Trader cpty = (Trader) cache.findRec(RecType.TRADER, trade.getCptyId());
        trade.enrich(trader, contr, cpty);
    }

    private final void enrichPosn(Posn posn) {
        final Trader trader = (Trader) cache.findRec(RecType.TRADER, posn.getTraderId());
        final Contr contr = (Contr) cache.findRec(RecType.CONTR, posn.getContrId());
        posn.enrich(trader, contr);
    }

    private final void insertOrder(Order order) {
        enrichOrder(order);
        final Market market = (Market) markets.find(Market.composeId(order.getContrId(),
                order.getSettlDay()));
        market.insertOrder(order);
        boolean success = false;
        try {
            final Accnt accnt = getLazyAccnt(order.getTrader());
            accnt.insertOrder(order);
            success = true;
        } finally {
            if (!success) {
                market.removeOrder(order);
            }
        }
    }

    private final void insertAssets() {
        model.selectAsset(new UnaryCallback<Asset>() {
            @Override
            public final void call(Asset arg) {
                cache.insertRec(arg);
            }
        });
    }

    private final void insertContrs() {
        model.selectContr(new UnaryCallback<Contr>() {
            @Override
            public final void call(Contr arg) {
                cache.insertRec(arg);
            }
        });
    }

    private final void insertTraders() {
        model.selectTrader(new UnaryCallback<Trader>() {
            @Override
            public final void call(Trader arg) {
                cache.insertRec(arg);
                emailIdx.insert(arg);
            }
        });
    }

    private final void insertMarkets() {
        model.selectMarket(new UnaryCallback<Market>() {
            @Override
            public final void call(Market arg) {
                enrichMarket(arg);
                markets.insert(arg);
            }
        });
    }

    private final void insertOrders() {
        model.selectOrder(new UnaryCallback<Order>() {
            @Override
            public final void call(Order arg) {
                insertOrder(arg);
            }
        });
    }

    private final void insertTrades() {
        model.selectTrade(new UnaryCallback<Exec>() {
            @Override
            public final void call(Exec arg) {
                enrichTrade(arg);
                final Accnt accnt = getLazyAccnt(arg.getTrader());
                accnt.insertTrade(arg);
            }
        });
    }

    private final void insertPosns() {
        model.selectPosn(new UnaryCallback<Posn>() {
            @Override
            public final void call(Posn arg) {
                enrichPosn(arg);
                final Accnt accnt = getLazyAccnt(arg.getTrader());
                accnt.insertPosn(arg);
            }
        });
    }

    private final Trader newTrader(String mnem, String display, String email) {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new IllegalArgumentException(String.format("invalid mnem '%s'", mnem));
        }
        final long traderId = model.allocTraderId();
        return new Trader(traderId, mnem, display, email);
    }

    private final Exec newExec(long id, Instruct instruct, long now) {
        return new Exec(id, instruct, now);
    }

    private static long spread(Order takerOrder, Order makerOrder, Direct direct) {
        return direct == Direct.PAID
        // Paid when the taker lifts the offer.
        ? makerOrder.getTicks() - takerOrder.getTicks()
                // Given when the taker hits the bid.
                : takerOrder.getTicks() - makerOrder.getTicks();
    }

    private final void matchOrders(Market market, Order takerOrder, Side side, Direct direct,
            Trans trans) {

        final long now = takerOrder.getCreated();

        long taken = 0;
        long lastTicks = 0;
        long lastLots = 0;

        final Contr contr = market.getContr();
        final int settlDay = market.getSettlDay();

        DlNode node = side.getFirstOrder();
        for (; taken < takerOrder.getResd() && !node.isEnd(); node = node.dlNext()) {
            final Order makerOrder = (Order) node;

            // Only consider orders while prices cross.
            if (spread(takerOrder, makerOrder, direct) > 0) {
                break;
            }

            final long makerId = market.allocExecId();
            final long takerId = market.allocExecId();

            final Accnt makerAccnt = findAccnt(makerOrder.getTrader());
            assert makerAccnt != null;
            final Posn makerPosn = makerAccnt.getLazyPosn(contr, settlDay);

            final Match match = new Match();
            match.makerOrder = makerOrder;
            match.makerPosn = makerPosn;
            match.ticks = makerOrder.getTicks();
            match.lots = Math.min(takerOrder.getResd() - taken, makerOrder.getResd());

            taken += match.lots;
            lastTicks = match.ticks;
            lastLots = match.lots;

            final Exec makerTrade = new Exec(makerId, makerOrder, now);
            makerTrade.trade(match.lots, match.ticks, match.lots, takerId, Role.MAKER,
                    takerOrder.getTrader());
            match.makerTrade = makerTrade;

            final Exec takerTrade = new Exec(takerId, takerOrder, now);
            takerTrade.trade(taken, match.ticks, match.lots, makerId, Role.TAKER,
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
            final Accnt takerAccnt = getLazyAccnt(takerOrder.getTrader());
            trans.posn = takerAccnt.getLazyPosn(contr, settlDay);
            takerOrder.trade(taken, lastTicks, lastLots, now);
        }
    }

    private final void matchOrders(Market market, Order order, Trans trans) {
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
        matchOrders(market, order, side, direct, trans);
    }

    // Assumes that maker lots have not been reduced since matching took place.

    private final void commitMatches(Accnt taker, Market market, long now, Trans trans) {
        for (SlNode node = trans.matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            final Order makerOrder = match.getMakerOrder();
            // Reduce maker.
            market.takeOrder(makerOrder, match.getLots(), now);
            // Must succeed because maker order exists.
            final Accnt maker = findAccnt(makerOrder.getTrader());
            assert maker != null;
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
        insertAssets();
        insertContrs();
        insertTraders();
        insertMarkets();
        insertOrders();
        insertTrades();
        insertPosns();
        // Build email index.
        for (SlNode node = cache.getFirstRec(RecType.TRADER); node != null; node = node.slNext()) {
            final Trader trader = (Trader) node;
            emailIdx.insert(trader);
        }
    }

    @Override
    public final void close() {
    }

    public final Trader createTrader(String mnem, String display, String email) {
        // Validate.
        final Trader trader = newTrader(mnem, display, email);
        model.insertTrader(trader);
        cache.insertRec(trader);
        emailIdx.insert(trader);
        return trader;
    }

    public final Rec findRec(RecType recType, long id) {
        return cache.findRec(recType, id);
    }

    public final Rec findRec(RecType recType, String mnem) {
        return cache.findRec(recType, mnem);
    }

    public final SlNode getFirstRec(RecType recType) {
        return cache.getFirstRec(recType);
    }

    public final boolean isEmptyRec(RecType recType) {
        return cache.isEmptyRec(recType);
    }

    public final Trader findTraderByEmail(String email) {
        return emailIdx.find(email);
    }

    public final Market createMarket(Contr contr, int settlDay, int expiryDay) {
        final long key = Market.composeId(contr.getId(), settlDay);
        final RbNode node = markets.pfind(key);
        if (node != null && node.getKey() == key) {
            throw new IllegalArgumentException(String.format("market '%s' for '%d' already exists",
                    contr.getMnem(), jdToIso(settlDay)));
        }
        final Market market = new Market(contr, settlDay, expiryDay);
        model.insertMarket(contr.getId(), settlDay, expiryDay);
        final RbNode parent = node;
        markets.pinsert(market, parent);
        return market;
    }

    public final Market getLazyMarket(Contr contr, int settlDay) {
        Market market;
        final long key = Market.composeId(contr.getId(), settlDay);
        final RbNode node = markets.pfind(key);
        if (node == null || node.getKey() != key) {
            market = new Market(contr, settlDay, settlDay);
            final RbNode parent = node;
            markets.pinsert(market, parent);
        } else {
            market = (Market) node;
        }
        return market;
    }

    public final Market getLazyMarket(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRec(RecType.CONTR, mnem);
        if (contr == null) {
            return null;
        }
        return getLazyMarket(contr, settlDay);
    }

    public final Market findMarket(Contr contr, int settlDay) {
        return (Market) markets.find(Market.composeId(contr.getId(), settlDay));
    }

    public final Market findMarket(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRec(RecType.CONTR, mnem);
        if (contr == null) {
            return null;
        }
        return findMarket(contr, settlDay);
    }

    public final RbNode getRootMarket() {
        return markets.getRoot();
    }

    public final RbNode getFirstMarket() {
        return markets.getFirst();
    }

    public final RbNode getLastMarket() {
        return markets.getLast();
    }

    public final boolean isEmptyMarket() {
        return markets.isEmpty();
    }

    public final Accnt getLazyAccnt(Trader trader) {
        Accnt accnt;
        final long key = trader.getId();
        final RbNode node = accnts.pfind(key);
        if (node == null || node.getKey() != key) {
            accnt = new Accnt(trader, refIdx);
            final RbNode parent = node;
            accnts.pinsert(accnt, parent);
        } else {
            accnt = (Accnt) node;
        }
        return accnt;
    }

    public final Accnt getLazyAccnt(String mnem) {
        final Trader trader = (Trader) cache.findRec(RecType.TRADER, mnem);
        if (trader == null) {
            return null;
        }
        return getLazyAccnt(trader);
    }

    public final Accnt getLazyAccntByEmail(String email) {
        final Trader trader = emailIdx.find(email);
        if (trader == null) {
            return null;
        }
        return getLazyAccnt(trader);
    }

    public final Accnt findAccnt(Trader trader) {
        return (Accnt) accnts.find(trader.getId());
    }

    public final Accnt findAccnt(String mnem) {
        final Trader trader = (Trader) cache.findRec(RecType.TRADER, mnem);
        if (trader == null) {
            return null;
        }
        return findAccnt(trader);
    }

    public final Accnt findAccntByEmail(String email) {
        final Trader trader = emailIdx.find(email);
        if (trader == null) {
            return null;
        }
        return findAccnt(trader);
    }

    public final Trans placeOrder(Accnt accnt, Market market, String ref, Action action,
            long ticks, long lots, long minLots, Trans trans) {
        if (lots == 0 || lots < minLots) {
            throw new IllegalArgumentException(String.format("invalid lots '%d'", lots));
        }
        final long now = System.currentTimeMillis();
        final long orderId = market.allocOrderId();
        final Contr contr = market.getContr();
        final int settlDay = market.getSettlDay();
        final Order order = new Order(orderId, accnt.getTrader(), contr, settlDay, ref, action,
                ticks, lots, minLots, now);
        final Exec exec = newExec(market.allocExecId(), order, now);

        trans.clear();
        trans.market = market;
        trans.order = order;
        trans.execs.insertBack(exec);
        // Order fields are updated on match.
        matchOrders(market, order, trans);
        // Place incomplete order in market.
        if (!order.isDone()) {
            // This may fail if level cannot be allocated.
            market.insertOrder(order);
        }
        // TODO: IOC orders would need an additional revision for the unsolicited cancellation of
        // any unfilled quantity.
        boolean success = false;
        try {
            model.insertExecList(contr.getId(), settlDay, (Exec) trans.execs.getFirst());
            success = true;
        } finally {
            if (!success && !order.isDone()) {
                // Undo market insertion.
                market.removeOrder(order);
            }
        }
        // Final commit phase cannot fail.
        accnt.insertOrder(order);
        // Commit trans to cycle and free matches.
        commitMatches(accnt, market, now, trans);
        return trans;
    }

    public final Trans reviseOrder(Accnt accnt, Market market, Order order, long lots, Trans trans) {
        if (order.isDone()) {
            throw new IllegalArgumentException(String.format("order '%d' is done", order.getId()));
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
        final Exec exec = newExec(market.allocExecId(), order, now);
        exec.revise(lots);
        model.insertExec(market.getContrId(), market.getSettlDay(), exec);

        // Final commit phase cannot fail.
        market.reviseOrder(order, lots, now);

        trans.clear();
        trans.market = market;
        trans.order = order;
        trans.execs.insertBack(exec);
        return trans;
    }

    public final Trans reviseOrder(Accnt accnt, Market market, long id, long lots, Trans trans) {
        final Contr contr = market.getContr();
        final int settlDay = market.getSettlDay();
        final Order order = accnt.findOrder(contr.getId(), settlDay, id);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%d'", id));
        }
        return reviseOrder(accnt, market, order, lots, trans);
    }

    public final Trans reviseOrder(Accnt accnt, Market market, String ref, long lots, Trans trans) {
        final Contr contr = market.getContr();
        final int settlDay = market.getSettlDay();
        final Order order = accnt.findOrder(contr.getId(), settlDay, ref);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%s'", ref));
        }
        return reviseOrder(accnt, market, order, lots, trans);
    }

    public final Trans cancelOrder(Accnt accnt, Market market, Order order, Trans trans) {
        if (order.isDone()) {
            throw new IllegalArgumentException(String.format("order '%d' is done", order.getId()));
        }
        final long now = System.currentTimeMillis();
        final Exec exec = newExec(market.allocExecId(), order, now);
        exec.cancel();
        model.insertExec(market.getContrId(), market.getSettlDay(), exec);

        // Final commit phase cannot fail.
        market.cancelOrder(order, now);

        trans.clear();
        trans.market = market;
        trans.order = order;
        trans.execs.insertBack(exec);
        return trans;
    }

    public final Trans cancelOrder(Accnt accnt, Market market, long id, Trans trans) {
        final Contr contr = market.getContr();
        final int settlDay = market.getSettlDay();
        final Order order = accnt.findOrder(contr.getId(), settlDay, id);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%d'", id));
        }
        return cancelOrder(accnt, market, order, trans);
    }

    public final Trans cancelOrder(Accnt accnt, Market market, String ref, Trans trans) {
        final Contr contr = market.getContr();
        final int settlDay = market.getSettlDay();
        final Order order = accnt.findOrder(contr.getId(), settlDay, ref);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%s'", ref));
        }
        return cancelOrder(accnt, market, order, trans);
    }

    /**
     * Cancels all orders.
     * 
     * This method is not executed atomically, so it may partially fail.
     * 
     * @param accnt
     *            the account.
     */
    public final void cancelOrder(Accnt accnt) {
        final long now = System.currentTimeMillis();
        for (;;) {
            final Order order = (Order) accnt.getRootOrder();
            if (order == null) {
                break;
            }
            final Market market = (Market) markets.find(Market.composeId(order.getContrId(),
                    order.getSettlDay()));
            assert market != null;

            final Exec exec = newExec(market.allocExecId(), order, now);
            exec.cancel();
            model.insertExec(market.getContrId(), market.getSettlDay(), exec);

            // Final commit phase cannot fail.
            market.cancelOrder(order, now);
        }
    }

    public final void archiveOrder(Accnt accnt, Order order) {
        if (!order.isDone()) {
            throw new IllegalArgumentException(String.format("order '%d' is not done",
                    order.getId()));
        }
        final long now = System.currentTimeMillis();
        model.archiveOrder(order.getContrId(), order.getSettlDay(), order.getId(), now);

        // No need to update timestamps on order because it is immediately freed.
        accnt.removeOrder(order);
    }

    public final void archiveOrder(Accnt accnt, long contrId, int settlDay, long id) {
        final Order order = accnt.findOrder(contrId, settlDay, id);
        if (order == null) {
            throw new IllegalArgumentException(String.format("no such order '%d'", id));
        }
        archiveOrder(accnt, order);
    }

    /**
     * Archive all orders.
     * 
     * This method is not updated atomically, so it may partially fail.
     * 
     * @param accnt
     *            the account.
     */
    public final void archiveOrder(Accnt accnt) {
        final long now = System.currentTimeMillis();
        RbNode node = accnt.getFirstOrder();
        while (node != null) {
            final Order order = (Order) node;
            // Move to next node before this order is unlinked.
            node = node.rbNext();
            if (!order.isDone()) {
                continue;
            }

            model.archiveOrder(order.getContrId(), order.getSettlDay(), order.getId(), now);

            // No need to update timestamps on order because it is immediately freed.
            accnt.removeOrder(order);
        }
    }

    public final void archiveTrade(Accnt accnt, Exec trade) {
        if (trade.getState() != State.TRADE) {
            throw new IllegalArgumentException(String.format("exec '%d' is not a trade",
                    trade.getId()));
        }
        final long now = System.currentTimeMillis();
        model.archiveTrade(trade.getContrId(), trade.getSettlDay(), trade.getId(), now);

        // No need to update timestamps on trade because it is immediately freed.
        accnt.removeTrade(trade);
    }

    public final void archiveTrade(Accnt accnt, long contrId, int settlDay, long id) {
        final Exec trade = accnt.findTrade(contrId, settlDay, id);
        if (trade == null) {
            throw new IllegalArgumentException(String.format("no such trade '%d'", id));
        }
        archiveTrade(accnt, trade);
    }

    /**
     * Archive all trades.
     * 
     * This method is not executed atomically, so it may partially fail.
     * 
     * @param accnt
     *            the account.
     */
    public final void archiveTrade(Accnt accnt) {
        final long now = System.currentTimeMillis();
        for (;;) {
            final Exec trade = (Exec) accnt.getRootTrade();
            if (trade == null) {
                break;
            }
            model.archiveTrade(trade.getContrId(), trade.getSettlDay(), trade.getId(), now);

            // No need to update timestamps on trade because it is immediately freed.
            accnt.removeTrade(trade);
        }
    }

    public final void archiveAll(Accnt accnt) {
        archiveOrder(accnt);
        archiveTrade(accnt);
    }
}

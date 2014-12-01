/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import java.util.regex.Pattern;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Direct;
import com.swirlycloud.domain.EmailIdx;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.domain.RefIdx;
import com.swirlycloud.domain.Role;
import com.swirlycloud.domain.Side;
import com.swirlycloud.domain.User;
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

    private final void insertOrder(Order order) {
        enrichOrder(order);
        final Market market = getLazyMarket(order.getContr(), order.getSettlDay());
        market.insertOrder(order);
        boolean success = false;
        try {
            final Accnt accnt = getLazyAccnt(order.getUser());
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

    private final void insertUsers() {
        model.selectUser(new UnaryCallback<User>() {
            @Override
            public final void call(User arg) {
                cache.insertRec(arg);
                emailIdx.insert(arg);
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
                final Accnt accnt = getLazyAccnt(arg.getUser());
                accnt.insertTrade(arg);
            }
        });
    }

    private final void insertPosns() {
        model.selectPosn(new UnaryCallback<Posn>() {
            @Override
            public final void call(Posn arg) {
                enrichPosn(arg);
                final Accnt accnt = getLazyAccnt(arg.getUser());
                accnt.insertPosn(arg);
            }
        });
    }

    private final User newUser(String mnem, String display, String email) {
        if (!MNEM_PATTERN.matcher(mnem).matches()) {
            throw new IllegalArgumentException(String.format("invalid mnem '%s'", mnem));
        }
        final long userId = model.allocIds(Kind.USER, 1);
        return new User(userId, mnem, display, email);
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
        insertAssets();
        insertContrs();
        insertUsers();
        insertOrders();
        insertTrades();
        insertPosns();
        // Build email index.
        for (SlNode node = cache.getFirstRec(Kind.USER); node != null; node = node.slNext()) {
            final User user = (User) node;
            emailIdx.insert(user);
        }
    }

    @Override
    public final void close() {
    }

    public final User registerUser(String mnem, String display, String email) {
        final User user = newUser(mnem, display, email);
        model.insertUser(user);
        cache.insertRec(user);
        emailIdx.insert(user);
        return user;
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

    public final User findUserByEmail(String email) {
        return emailIdx.find(email);
    }

    public final Market getLazyMarket(Contr contr, int settlDay) {
        Market market;
        final long id = Market.toId(contr.getId(), settlDay);
        final RbNode node = markets.pfind(id);
        if (node == null || node.getId() != id) {
            market = new Market(contr, settlDay);
            final RbNode parent = node;
            markets.pinsert(market, parent);
        } else {
            market = (Market) node;
        }
        return market;
    }

    public final Market getLazyMarket(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRec(Kind.CONTR, mnem);
        if (contr == null) {
            throw new IllegalArgumentException(String.format("invalid contr '%s'", mnem));
        }
        return getLazyMarket(contr, settlDay);
    }

    public final Market findMarket(Contr contr, int settlDay) {
        return (Market) markets.find(Market.toId(contr.getId(), settlDay));
    }

    public final Market findMarket(String mnem, int settlDay) {
        final Contr contr = (Contr) cache.findRec(Kind.CONTR, mnem);
        if (contr == null) {
            throw new IllegalArgumentException(String.format("invalid contr '%s'", mnem));
        }
        return findMarket(contr, settlDay);
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

    public final Accnt getLazyAccntByEmail(String email) {
        final User user = emailIdx.find(email);
        if (user == null) {
            throw new IllegalArgumentException(String.format("invalid user '%s'", email));
        }
        return getLazyAccnt(user);
    }

    public final Trans placeOrder(Accnt accnt, Market market, String ref, Action action,
            long ticks, long lots, long minLots, Trans trans) {
        if (lots == 0 || lots < minLots) {
            throw new IllegalArgumentException(String.format("invalid lots '%d'", lots));
        }
        final long now = System.currentTimeMillis();
        final long orderId = model.allocIds(Kind.ORDER, 1);
        final Contr contr = market.getContr();
        final int settlDay = market.getSettlDay();
        final Order order = new Order(orderId, accnt.getUser(), contr, settlDay, ref, action,
                ticks, lots, minLots, now);
        final Exec exec = newExec(order, now);

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
            model.insertExecList(market.getId(), (Exec) trans.execs.getFirst());
            success = true;
        } finally {
            if (!success && !order.isDone()) {
                // Undo market insertion.
                market.removeOrder(order);
            }
        }
        // Final commit phase cannot fail.
        if (!order.isDone()) {
            accnt.insertOrder(order);
        }
        // Commit trans to cycle and free matches.
        commitMatches(accnt, market, now, trans);
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
        final Market market = findMarket(order.getContr(), order.getSettlDay());
        assert market != null;

        final long now = System.currentTimeMillis();
        final Exec exec = newExec(order, now);
        exec.revise(lots);
        model.insertExec(market.getId(), exec);

        // Final commit phase cannot fail.
        market.reviseOrder(order, lots, now);

        trans.clear();
        trans.market = market;
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
        final Market market = findMarket(order.getContr(), order.getSettlDay());
        assert market != null;

        final long now = System.currentTimeMillis();
        final Exec exec = newExec(order, now);
        exec.cancel();
        model.insertExec(market.getId(), exec);

        // Final commit phase cannot fail.
        market.cancelOrder(order, now);
        accnt.removeOrder(order);

        trans.clear();
        trans.market = market;
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
        model.updateExec(Market.toId(trade.getContrId(), trade.getSettlDay()), id, now);

        // No need to update timestamps on trade because it is immediately freed.
        accnt.removeTrade(trade);
    }
}

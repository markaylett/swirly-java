/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import static com.swirlycloud.twirly.util.MnemUtil.newMnem;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.Container;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.PosnTree;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.mock.MockAsset;
import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Memorable;

public class AppEngineModel implements Model {

    protected static final String ASSET_KIND = "Asset";
    protected static final String CONTR_KIND = "Contr";
    protected static final String MARKET_KIND = "Market";
    protected static final String TRADER_KIND = "Trader";
    protected static final String ORDER_KIND = "Order";
    protected static final String EXEC_KIND = "Exec";

    private final Factory factory;
    private final MockAsset mockAsset;
    private final MockContr mockContr;

    protected final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private static int intOrZeroIfNull(Object o) {
        return o != null ? ((Long) o).intValue() : 0;
    }

    private static long longOrZeroIfNull(Object o) {
        return o != null ? ((Long) o).longValue() : 0;
    }

    private final void foreachMarket(UnaryCallback<Entity> cb) {
        final Query query = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(query);
        for (final Entity entity : pq.asIterable()) {
            cb.call(entity);
        }
    }

    private final void selectOrder(@NonNull final Filter filter,
            @NonNull final Container<? super Order> c) {
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query query = new Query(ORDER_KIND, arg.getKey()).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(query);
                for (final Entity entity : pq.asIterable()) {
                    final long id = entity.getKey().getId();
                    final String trader = (String) entity.getProperty("trader");
                    final String market = (String) entity.getProperty("market");
                    final String contr = (String) entity.getProperty("contr");
                    final int settlDay = intOrZeroIfNull(entity.getProperty("settlDay"));
                    final String ref = (String) entity.getProperty("ref");
                    @SuppressWarnings("null")
                    final State state = State.valueOf((String) entity.getProperty("state"));
                    @SuppressWarnings("null")
                    final Side side = Side.valueOf((String) entity.getProperty("side"));
                    final long ticks = (Long) entity.getProperty("ticks");
                    final long lots = (Long) entity.getProperty("lots");
                    final long resd = (Long) entity.getProperty("resd");
                    final long exec = (Long) entity.getProperty("exec");
                    final long cost = (Long) entity.getProperty("cost");
                    final long lastTicks = longOrZeroIfNull(entity.getProperty("lastTicks"));
                    final long lastLots = longOrZeroIfNull(entity.getProperty("lastLots"));
                    final long minLots = (Long) entity.getProperty("minLots");
                    final long created = (Long) entity.getProperty("created");
                    final long modified = (Long) entity.getProperty("modified");

                    assert trader != null;
                    assert market != null;
                    assert contr != null;
                    final Order order = factory.newOrder(id, trader, market, contr, settlDay, ref,
                            state, side, ticks, lots, resd, exec, cost, lastTicks, lastLots,
                            minLots, created, modified);
                    c.add(order);
                }
            }
        });
    }

    private final void selectTrade(@NonNull final Filter filter,
            @NonNull final Container<? super Exec> c) {
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query query = new Query(EXEC_KIND, arg.getKey()).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(query);
                for (final Entity entity : pq.asIterable()) {
                    final long id = entity.getKey().getId();
                    final long orderId = (Long) entity.getProperty("orderId");
                    final String trader = (String) entity.getProperty("trader");
                    final String market = (String) entity.getProperty("market");
                    final String contr = (String) entity.getProperty("contr");
                    final int settlDay = intOrZeroIfNull(entity.getProperty("settlDay"));
                    final String ref = (String) entity.getProperty("ref");
                    @SuppressWarnings("null")
                    final State state = State.valueOf((String) entity.getProperty("state"));
                    @SuppressWarnings("null")
                    final Side side = Side.valueOf((String) entity.getProperty("side"));
                    final long ticks = (Long) entity.getProperty("ticks");
                    final long lots = (Long) entity.getProperty("lots");
                    final long resd = (Long) entity.getProperty("resd");
                    final long exec = (Long) entity.getProperty("exec");
                    final long cost = (Long) entity.getProperty("cost");
                    final long lastTicks = longOrZeroIfNull(entity.getProperty("lastTicks"));
                    final long lastLots = longOrZeroIfNull(entity.getProperty("lastLots"));
                    final long minLots = (Long) entity.getProperty("minLots");
                    final long matchId = (Long) entity.getProperty("matchId");
                    final String s = (String) entity.getProperty("role");
                    final Role role = s != null ? Role.valueOf(s) : null;
                    final String cpty = (String) entity.getProperty("cpty");
                    final long created = (Long) entity.getProperty("created");

                    assert trader != null;
                    assert market != null;
                    assert contr != null;
                    final Exec trade = factory.newExec(id, orderId, trader, market, contr,
                            settlDay, ref, state, side, ticks, lots, resd, exec, cost, lastTicks,
                            lastLots, minLots, matchId, role, cpty, created);
                    c.add(trade);
                }
            }
        });
    }

    private final void selectPosn(@NonNull final Filter filter, final int busDay,
            @NonNull final Container<? super Posn> c) {
        final PosnTree posns = new PosnTree();
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query query = new Query(EXEC_KIND).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(query);
                for (final Entity entity : pq.asIterable()) {
                    final String trader = (String) entity.getProperty("trader");
                    final String contr = (String) entity.getProperty("contr");
                    int settlDay = intOrZeroIfNull(entity.getProperty("settlDay"));
                    assert trader != null;
                    assert contr != null;
                    // FIXME: Consider time-of-day.
                    if (settlDay != 0 && settlDay <= busDay) {
                        settlDay = 0;
                    }
                    // Lazy position.
                    Posn posn = (Posn) posns.pfind(trader, contr, settlDay);
                    if (posn == null || !posn.getTrader().equals(trader)
                            || !posn.getContr().equals(contr) || posn.getSettlDay() != settlDay) {
                        final RbNode parent = posn;
                        assert trader != null;
                        assert contr != null;
                        posn = factory.newPosn(trader, contr, settlDay);
                        posns.pinsert(posn, parent);
                    }
                    @SuppressWarnings("null")
                    final Side side = Side.valueOf((String) entity.getProperty("side"));
                    final long lastTicks = longOrZeroIfNull(entity.getProperty("lastTicks"));
                    final long lastLots = longOrZeroIfNull(entity.getProperty("lastLots"));
                    posn.addTrade(side, lastTicks, lastLots);
                }
            }
        });
        for (;;) {
            final Posn posn = (Posn) posns.getRoot();
            if (posn == null) {
                break;
            }
            posns.remove(posn);
            c.add(posn);
        }
    }

    public AppEngineModel(Factory factory) {
        this.factory = factory;
        mockAsset = new MockAsset(factory);
        mockContr = new MockContr(factory);
    }

    @Override
    public void close() {
    }

    @Override
    public final @NonNull MnemRbTree selectAsset() {
        // TODO: migrate to datastore.
        return mockAsset.selectAsset();
    }

    @Override
    public final @Nullable MnemRbTree selectContr() {
        // TODO: migrate to datastore.
        return mockContr.selectContr();
    }

    @Override
    public final @Nullable MnemRbTree selectMarket() {
        final MnemRbTree t = new MnemRbTree();
        final Query query = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(query);
        for (final Entity entity : pq.asIterable()) {
            final String mnem = entity.getKey().getName();
            final String display = (String) entity.getProperty("display");
            @SuppressWarnings("null")
            final Memorable contr = newMnem((String) entity.getProperty("contr"));
            final int settlDay = intOrZeroIfNull(entity.getProperty("settlDay"));
            final int expiryDay = intOrZeroIfNull(entity.getProperty("expiryDay"));
            final int state = ((Long) entity.getProperty("state")).intValue();
            final long lastTicks = longOrZeroIfNull(entity.getProperty("lastTicks"));
            final long lastLots = longOrZeroIfNull(entity.getProperty("lastLots"));
            final long lastTime = longOrZeroIfNull(entity.getProperty("lastTime"));
            final long maxOrderId = (Long) entity.getProperty("maxOrderId");
            final long maxExecId = (Long) entity.getProperty("maxExecId");

            assert mnem != null;
            final Market market = factory.newMarket(mnem, display, contr, settlDay, expiryDay,
                    state, lastTicks, lastLots, lastTime, maxOrderId, maxExecId);
            t.insert(market);
        }
        return t;
    }

    @Override
    public final @Nullable MnemRbTree selectTrader() {
        final MnemRbTree t = new MnemRbTree();
        final Query query = new Query(TRADER_KIND);
        final PreparedQuery pq = datastore.prepare(query);
        for (final Entity entity : pq.asIterable()) {
            final String mnem = entity.getKey().getName();
            final String display = (String) entity.getProperty("display");
            final String email = (String) entity.getProperty("email");

            assert mnem != null;
            assert email != null;
            final Trader trader = factory.newTrader(mnem, display, email);
            t.insert(trader);
        }
        return t;
    }

    @Override
    public final @Nullable String selectTraderByEmail(@NonNull String email)
            throws InterruptedException {
        final Filter filter = new FilterPredicate("email", FilterOperator.EQUAL, email);
        final Query query = new Query(TRADER_KIND).setFilter(filter).setKeysOnly();
        final PreparedQuery pq = datastore.prepare(query);
        final Entity entity = pq.asSingleEntity();
        return entity != null ? entity.getKey().getName() : null;
    }

    @Override
    public final @Nullable SlNode selectOrder() {
        final Filter filter = new FilterPredicate("archive", FilterOperator.EQUAL, Boolean.FALSE);
        final SlQueue q = new SlQueue();
        selectOrder(filter, q);
        return q.getFirst();
    }

    @Override
    public final @Nullable InstructTree selectOrder(@NonNull String trader) {
        final Filter traderFilter = new FilterPredicate("trader", FilterOperator.EQUAL, trader);
        final Filter archiveFilter = new FilterPredicate("archive", FilterOperator.EQUAL,
                Boolean.FALSE);
        final Filter filter = CompositeFilterOperator.and(traderFilter, archiveFilter);
        assert filter != null;
        final InstructTree t = new InstructTree();
        selectOrder(filter, t);
        return t;
    }

    @Override
    public final @Nullable SlNode selectTrade() {
        final Filter stateFilter = new FilterPredicate("state", FilterOperator.EQUAL,
                State.TRADE.name());
        final Filter archiveFilter = new FilterPredicate("archive", FilterOperator.EQUAL,
                Boolean.FALSE);
        final Filter filter = CompositeFilterOperator.and(stateFilter, archiveFilter);
        assert filter != null;
        final SlQueue q = new SlQueue();
        selectTrade(filter, q);
        return q.getFirst();

    }

    @Override
    public final @Nullable InstructTree selectTrade(@NonNull String trader) {
        final Filter traderFilter = new FilterPredicate("trader", FilterOperator.EQUAL, trader);
        final Filter stateFilter = new FilterPredicate("state", FilterOperator.EQUAL,
                State.TRADE.name());
        final Filter archiveFilter = new FilterPredicate("archive", FilterOperator.EQUAL,
                Boolean.FALSE);
        final Filter filter = CompositeFilterOperator.and(traderFilter, stateFilter, archiveFilter);
        assert filter != null;
        final InstructTree t = new InstructTree();
        selectTrade(filter, t);
        return t;

    }

    @Override
    public final @Nullable SlNode selectPosn(final int busDay) {
        final Filter filter = new FilterPredicate("state", FilterOperator.EQUAL, State.TRADE.name());
        final SlQueue q = new SlQueue();
        selectPosn(filter, busDay, q);
        return q.getFirst();

    }

    @Override
    public final @Nullable TraderPosnTree selectPosn(@NonNull String trader, final int busDay) {
        final Filter traderFilter = new FilterPredicate("trader", FilterOperator.EQUAL, trader);
        final Filter stateFilter = new FilterPredicate("state", FilterOperator.EQUAL,
                State.TRADE.name());
        final Filter filter = CompositeFilterOperator.and(traderFilter, stateFilter);
        assert filter != null;
        final TraderPosnTree t = new TraderPosnTree();
        selectPosn(filter, busDay, t);
        return t;
    }
}

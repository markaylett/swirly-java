/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import static com.swirlycloud.twirly.node.SlUtil.popNext;
import static com.swirlycloud.twirly.util.MnemUtil.newMnem;
import static com.swirlycloud.twirly.util.NullUtil.nullIfZero;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.PosnTree;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.mock.MockAsset;
import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Memorable;

public final class DatastoreModel implements Model {

    @SuppressWarnings("unused")
    private static final String ASSET_KIND = "Asset";
    @SuppressWarnings("unused")
    private static final String CONTR_KIND = "Contr";
    private static final String MARKET_KIND = "Market";
    private static final String TRADER_KIND = "Trader";
    private static final String ORDER_KIND = "Order";
    private static final String EXEC_KIND = "Exec";

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private static int intOrZeroIfNull(Object o) {
        return o != null ? ((Long) o).intValue() : 0;
    }

    private static long longOrZeroIfNull(Object o) {
        return o != null ? ((Long) o).longValue() : 0;
    }

    private final void updateMaxOrderId(Entity market, long id) {
        Long maxOrderId = (Long) market.getProperty("maxOrderId");
        if (maxOrderId.longValue() < id) {
            maxOrderId = Long.valueOf(id);
            market.setUnindexedProperty("maxOrderId", maxOrderId);
        }
    }

    private final void updateMaxExecId(Entity market, long id) {
        Long maxExecId = (Long) market.getProperty("maxExecId");
        if (maxExecId.longValue() < id) {
            maxExecId = Long.valueOf(id);
            market.setUnindexedProperty("maxExecId", maxExecId);
        }
    }

    private final Entity newMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state) {
        final Long zero = Long.valueOf(0);
        final Entity entity = new Entity(MARKET_KIND, mnem);
        entity.setUnindexedProperty("display", display);
        entity.setUnindexedProperty("contr", contr);
        entity.setUnindexedProperty("settlDay", nullIfZero(settlDay));
        entity.setUnindexedProperty("expiryDay", nullIfZero(expiryDay));
        entity.setUnindexedProperty("state", state);
        entity.setUnindexedProperty("lastTicks", null);
        entity.setUnindexedProperty("lastLots", null);
        entity.setUnindexedProperty("lastTime", null);
        entity.setUnindexedProperty("maxOrderId", zero);
        entity.setUnindexedProperty("maxExecId", zero);
        return entity;
    }

    private final Entity newTrader(String mnem, String display, String email) {
        final Entity entity = new Entity(TRADER_KIND, mnem);
        entity.setUnindexedProperty("display", display);
        entity.setProperty("email", email);
        return entity;
    }

    private final Entity newOrder(Entity market, Exec exec) {
        final Entity entity = new Entity(ORDER_KIND, exec.getOrderId(), market.getKey());
        entity.setUnindexedProperty("trader", exec.getTrader());
        entity.setUnindexedProperty("market", exec.getMarket());
        entity.setUnindexedProperty("contr", exec.getContr());
        entity.setUnindexedProperty("settlDay", nullIfZero(exec.getSettlDay()));
        entity.setUnindexedProperty("ref", exec.getRef());
        entity.setUnindexedProperty("state", exec.getState().name());
        entity.setUnindexedProperty("action", exec.getAction().name());
        entity.setUnindexedProperty("ticks", exec.getTicks());
        entity.setUnindexedProperty("lots", exec.getLots());
        entity.setProperty("resd", exec.getResd());
        entity.setUnindexedProperty("exec", exec.getExec());
        entity.setUnindexedProperty("cost", exec.getCost());
        if (exec.getLastLots() > 0) {
            entity.setUnindexedProperty("lastTicks", exec.getLastTicks());
            entity.setUnindexedProperty("lastLots", exec.getLastLots());
        } else {
            entity.setUnindexedProperty("lastTicks", null);
            entity.setUnindexedProperty("lastLots", null);
        }
        entity.setUnindexedProperty("minLots", exec.getMinLots());
        entity.setProperty("archive", Boolean.FALSE);
        entity.setUnindexedProperty("created", exec.getCreated());
        entity.setUnindexedProperty("modified", exec.getCreated());
        updateMaxOrderId(market, exec.getOrderId());
        return entity;
    }

    private final Entity newExec(Entity market, Exec exec) {
        final Entity entity = new Entity(EXEC_KIND, exec.getId(), market.getKey());
        entity.setUnindexedProperty("orderId", nullIfZero(exec.getOrderId()));
        entity.setUnindexedProperty("trader", exec.getTrader());
        entity.setUnindexedProperty("market", exec.getMarket());
        entity.setUnindexedProperty("contr", exec.getContr());
        entity.setUnindexedProperty("settlDay", nullIfZero(exec.getSettlDay()));
        entity.setUnindexedProperty("ref", exec.getRef());
        entity.setProperty("state", exec.getState().name());
        entity.setUnindexedProperty("action", exec.getAction().name());
        entity.setUnindexedProperty("ticks", exec.getTicks());
        entity.setUnindexedProperty("lots", exec.getLots());
        entity.setUnindexedProperty("resd", exec.getResd());
        entity.setUnindexedProperty("exec", exec.getExec());
        entity.setUnindexedProperty("cost", exec.getCost());
        if (exec.getLastLots() > 0) {
            entity.setUnindexedProperty("lastTicks", exec.getLastTicks());
            entity.setUnindexedProperty("lastLots", exec.getLastLots());
        } else {
            entity.setUnindexedProperty("lastTicks", null);
            entity.setUnindexedProperty("lastLots", null);
        }
        entity.setUnindexedProperty("minLots", exec.getMinLots());
        entity.setUnindexedProperty("matchId", nullIfZero(exec.getMatchId()));
        final Role role = exec.getRole();
        entity.setUnindexedProperty("role", role != null ? role.name() : null);
        entity.setUnindexedProperty("cpty", exec.getCpty());
        entity.setProperty("archive", Boolean.FALSE);
        entity.setUnindexedProperty("created", exec.getCreated());
        entity.setUnindexedProperty("modified", exec.getCreated());
        // Update market.
        if (exec.getState() == State.TRADE && exec.isAuto()) {
            market.setUnindexedProperty("lastTicks", exec.getLastTicks());
            market.setUnindexedProperty("lastLots", exec.getLastLots());
            market.setUnindexedProperty("lastTime", exec.getCreated());
        }
        updateMaxExecId(market, exec.getId());
        return entity;
    }

    private final Entity applyExec(Entity order, Exec exec) {
        order.setProperty("state", exec.getState().name());
        order.setUnindexedProperty("lots", exec.getLots());
        order.setProperty("resd", exec.getResd());
        order.setUnindexedProperty("exec", exec.getExec());
        order.setUnindexedProperty("cost", exec.getCost());
        if (exec.getLastLots() > 0) {
            order.setUnindexedProperty("lastTicks", exec.getLastTicks());
            order.setUnindexedProperty("lastLots", exec.getLastLots());
        }
        order.setUnindexedProperty("modified", exec.getCreated());
        return order;
    }

    private final Entity getMarket(Transaction txn, String market) throws NotFoundException {
        final Key key = KeyFactory.createKey(MARKET_KIND, market);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new NotFoundException(String.format("market '%s' does not exist in datastore",
                    market));
        }
    }

    private final Entity getTrader(Transaction txn, String trader) throws NotFoundException {
        final Key key = KeyFactory.createKey(TRADER_KIND, trader);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new NotFoundException(String.format("trader '%s' does not exist in datastore",
                    trader));
        }
    }

    private final Entity getOrder(Transaction txn, Key parent, long id) throws NotFoundException {
        final Key key = KeyFactory.createKey(parent, ORDER_KIND, id);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new NotFoundException(String.format("order '%d' does not exist in datastore", id));
        }
    }

    private final Entity getExec(Transaction txn, Key parent, long id) throws NotFoundException {
        final Key key = KeyFactory.createKey(parent, EXEC_KIND, id);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new NotFoundException(String.format("exec '%d' does not exist in datastore", id));
        }
    }

    private final void foreachMarket(UnaryCallback<Entity> cb) {
        final Query query = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(query);
        for (final Entity entity : pq.asIterable()) {
            cb.call(entity);
        }
    }

    @Override
    public final void close() {
    }

    @Override
    public final void insertMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state) {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = newMarket(mnem, display, contr, settlDay, expiryDay, state);
            datastore.put(txn, entity);
            txn.commit();
        } catch (ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void updateMarket(String mnem, String display, int state) throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = getMarket(txn, mnem);
            entity.setUnindexedProperty("display", display);
            entity.setUnindexedProperty("state", state);
            datastore.put(txn, entity);
            txn.commit();
        } catch (ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void insertTrader(String mnem, String display, String email) {
        final Transaction txn = datastore.beginTransaction();
        try {
            // Trader entities have common ancestor for strong consistency.
            final Entity entity = newTrader(mnem, display, email);
            datastore.put(txn, entity);
            txn.commit();
        } catch (ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void updateTrader(String mnem, String display) throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = getTrader(txn, mnem);
            entity.setUnindexedProperty("display", display);
            datastore.put(txn, entity);
            txn.commit();
        } catch (ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void insertExec(Exec exec) throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, exec.getMarket());
            final long orderId = exec.getOrderId();
            if (orderId != 0) {
                if (exec.getState() == State.NEW) {
                    datastore.put(txn, newOrder(market, exec));
                } else {
                    datastore.put(txn, applyExec(getOrder(txn, market.getKey(), orderId), exec));
                }
            }
            datastore.put(txn, newExec(market, exec));
            datastore.put(txn, market);
            txn.commit();
        } catch (ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void insertExecList(String marketMnem, SlNode first) throws NotFoundException {
        SlNode node = first;
        try {
            // N.B. the approach I used previously on a traditional RDMS was quite different, in
            // that order revisions were managed as triggers on the exec table.
            final Map<Long, Entity> orders = new HashMap<>();
            final Transaction txn = datastore.beginTransaction();
            try {
                final Entity market = getMarket(txn, marketMnem);
                while (node != null) {
                    final Exec exec = (Exec) node;
                    node = popNext(node);

                    final long orderId = exec.getOrderId();
                    if (orderId != 0) {
                        if (exec.getState() == State.NEW) {
                            // Defer actual datastore put.
                            orders.put(orderId, newOrder(market, exec));
                        } else {
                            // This exec may apply to a cached order.
                            Entity order = orders.get(orderId);
                            if (order == null) {
                                // Otherwise fetch the order from the datastore.
                                order = getOrder(txn, market.getKey(), orderId);
                                orders.put(orderId, order);
                            }
                            applyExec(order, exec);
                        }
                    }
                    datastore.put(txn, newExec(market, exec));
                }
                if (!orders.isEmpty()) {
                    datastore.put(txn, orders.values());
                }
                datastore.put(txn, market);
                txn.commit();
            } catch (ConcurrentModificationException e) {
                // FIXME: implement retry logic.
                throw e;
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        } finally {
            // Clear nodes to ensure no unwanted retention.
            while (node != null) {
                node = popNext(node);
            }
        }
    }

    @Override
    public final void archiveOrder(String marketMnem, long id, long modified)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, marketMnem);
            final Entity entity = getOrder(txn, market.getKey(), id);
            entity.setProperty("archive", Boolean.TRUE);
            entity.setUnindexedProperty("modified", modified);
            datastore.put(txn, entity);
            txn.commit();
        } catch (ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void archiveTrade(String marketMnem, long id, long modified)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, marketMnem);
            final Entity entity = getExec(txn, market.getKey(), id);
            entity.setProperty("archive", Boolean.TRUE);
            entity.setUnindexedProperty("modified", modified);
            datastore.put(txn, entity);
            txn.commit();
        } catch (ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final @Nullable SlNode selectAsset() {
        // TODO: migrate to datastore.
        return MockAsset.selectAsset();
    }

    @Override
    public final @Nullable SlNode selectContr() {
        // TODO: migrate to datastore.
        return MockContr.selectContr();
    }

    @Override
    public final @Nullable SlNode selectMarket() {
        final SlQueue q = new SlQueue();
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
            final Market market = new Market(mnem, display, contr, settlDay, expiryDay, state,
                    lastTicks, lastLots, lastTime, maxOrderId, maxExecId);
            q.insertBack(market);
        }
        return q.getFirst();
    }

    @Override
    public final @Nullable SlNode selectTrader() {
        final SlQueue q = new SlQueue();
        final Query query = new Query(TRADER_KIND);
        final PreparedQuery pq = datastore.prepare(query);
        for (final Entity entity : pq.asIterable()) {
            final String mnem = entity.getKey().getName();
            final String display = (String) entity.getProperty("display");
            final String email = (String) entity.getProperty("email");

            assert mnem != null;
            assert email != null;
            final Trader trader = new Trader(mnem, display, email);
            q.insertBack(trader);
        }
        return q.getFirst();
    }

    @Override
    public final @Nullable SlNode selectOrder() {
        final SlQueue q = new SlQueue();
        final Filter filter = new FilterPredicate("archive", FilterOperator.EQUAL, Boolean.FALSE);
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
                    final Action action = Action.valueOf((String) entity.getProperty("action"));
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
                    final Order order = new Order(id, trader, market, contr, settlDay, ref, state,
                            action, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots,
                            created, modified);
                    q.insertBack(order);
                }
            }
        });
        return q.getFirst();
    }

    @Override
    public final @Nullable SlNode selectTrade() {
        final SlQueue q = new SlQueue();
        final Filter stateFilter = new FilterPredicate("state", FilterOperator.EQUAL,
                State.TRADE.name());
        final Filter archiveFilter = new FilterPredicate("archive", FilterOperator.EQUAL,
                Boolean.FALSE);
        final Filter filter = CompositeFilterOperator.and(stateFilter, archiveFilter);
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
                    final Action action = Action.valueOf((String) entity.getProperty("action"));
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
                    final Exec trade = new Exec(id, orderId, trader, market, contr, settlDay, ref,
                            state, action, ticks, lots, resd, exec, cost, lastTicks, lastLots,
                            minLots, matchId, role, cpty, created);
                    q.insertBack(trade);
                }
            }
        });
        return q.getFirst();
    }

    @Override
    public final @Nullable SlNode selectPosn() {
        final PosnTree posns = new PosnTree();
        final Filter filter = new FilterPredicate("state", FilterOperator.EQUAL, State.TRADE.name());
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query query = new Query(EXEC_KIND).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(query);
                for (final Entity entity : pq.asIterable()) {
                    final String trader = (String) entity.getProperty("trader");
                    final String contr = (String) entity.getProperty("contr");
                    final int settlDay = intOrZeroIfNull(entity.getProperty("settlDay"));
                    // FIXME: handle settled contracts.
                    assert trader != null;
                    assert contr != null;
                    // Lazy position.
                    Posn posn = (Posn) posns.pfind(trader, contr, settlDay);
                    if (posn == null || !posn.getTrader().equals(trader)
                            || !posn.getContr().equals(contr) || posn.getSettlDay() != settlDay) {
                        final RbNode parent = posn;
                        assert trader != null;
                        assert contr != null;
                        posn = new Posn(trader, contr, settlDay);
                        posns.pinsert(posn, parent);
                    }
                    @SuppressWarnings("null")
                    final Action action = Action.valueOf((String) entity.getProperty("action"));
                    final long lastTicks = longOrZeroIfNull(entity.getProperty("lastTicks"));
                    final long lastLots = longOrZeroIfNull(entity.getProperty("lastLots"));
                    posn.addTrade(action, lastTicks, lastLots);
                }
            }
        });
        final SlQueue q = new SlQueue();
        for (;;) {
            final Posn posn = (Posn) posns.getRoot();
            if (posn == null) {
                break;
            }
            posns.remove(posn);
            q.insertBack(posn);
        }
        return q.getFirst();
    }
}

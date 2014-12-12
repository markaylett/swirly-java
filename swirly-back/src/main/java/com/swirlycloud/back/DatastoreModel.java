/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.util.AshUtil.newId;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Role;
import com.swirlycloud.domain.State;
import com.swirlycloud.domain.Trader;
import com.swirlycloud.engine.Model;
import com.swirlycloud.exception.NotFoundException;
import com.swirlycloud.function.UnaryCallback;
import com.swirlycloud.mock.MockAsset;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.SlNode;

public final class DatastoreModel implements Model {

    private static final String GROUP_KIND = "Group";
    @SuppressWarnings("unused")
    private static final String ASSET_KIND = "Asset";
    @SuppressWarnings("unused")
    private static final String CONTR_KIND = "Contr";
    private static final String TRADER_KIND = "Trader";
    private static final String TRADER_MNEM_KIND = "TraderMnem";
    private static final String TRADER_EMAIL_KIND = "TraderEmail";
    private static final String MARKET_KIND = "Market";
    private static final String ORDER_KIND = "Order";
    private static final String EXEC_KIND = "Exec";

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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

    private final Entity newTrader(Entity group, Trader trader) {
        final Entity entity = new Entity(TRADER_KIND, trader.getId(), group.getKey());
        entity.setUnindexedProperty("mnem", trader.getMnem());
        entity.setUnindexedProperty("display", trader.getDisplay());
        entity.setUnindexedProperty("email", trader.getEmail());
        return entity;
    }

    private final Entity newOrder(Entity market, Exec exec) {
        final Entity entity = new Entity(ORDER_KIND, exec.getOrderId(), market.getKey());
        entity.setUnindexedProperty("traderId", exec.getTraderId());
        entity.setUnindexedProperty("contrId", exec.getContrId());
        entity.setUnindexedProperty("settlDay", Integer.valueOf(exec.getSettlDay()));
        entity.setUnindexedProperty("ref", exec.getRef());
        entity.setUnindexedProperty("state", exec.getState().name());
        entity.setUnindexedProperty("action", exec.getAction().name());
        entity.setUnindexedProperty("ticks", exec.getTicks());
        entity.setUnindexedProperty("lots", exec.getLots());
        entity.setProperty("resd", exec.getResd());
        entity.setUnindexedProperty("exec", exec.getExec());
        entity.setUnindexedProperty("lastTicks", exec.getLastTicks());
        entity.setUnindexedProperty("lastLots", exec.getLastLots());
        entity.setUnindexedProperty("minLots", exec.getMinLots());
        entity.setProperty("archive", Boolean.FALSE);
        entity.setUnindexedProperty("created", exec.getCreated());
        entity.setUnindexedProperty("modified", exec.getCreated());
        updateMaxOrderId(market, exec.getOrderId());
        return entity;
    }

    private final Entity newExec(Entity market, Exec exec) {
        final Entity entity = new Entity(EXEC_KIND, exec.getId(), market.getKey());
        entity.setUnindexedProperty("orderId", exec.getOrderId());
        entity.setUnindexedProperty("traderId", exec.getTraderId());
        entity.setUnindexedProperty("contrId", exec.getContrId());
        entity.setUnindexedProperty("settlDay", Integer.valueOf(exec.getSettlDay()));
        entity.setUnindexedProperty("ref", exec.getRef());
        entity.setProperty("state", exec.getState().name());
        entity.setUnindexedProperty("action", exec.getAction().name());
        entity.setUnindexedProperty("ticks", exec.getTicks());
        entity.setUnindexedProperty("lots", exec.getLots());
        entity.setUnindexedProperty("resd", exec.getResd());
        entity.setUnindexedProperty("exec", exec.getExec());
        entity.setUnindexedProperty("lastTicks", exec.getLastTicks());
        entity.setUnindexedProperty("lastLots", exec.getLastLots());
        entity.setUnindexedProperty("minLots", exec.getMinLots());
        if (exec.getState() == State.TRADE) {
            entity.setUnindexedProperty("matchId", exec.getMatchId());
            entity.setUnindexedProperty("role", exec.getRole().name());
            entity.setUnindexedProperty("cptyId", exec.getCptyId());
            market.setUnindexedProperty("lastTicks", exec.getLastTicks());
            market.setUnindexedProperty("lastLots", exec.getLastLots());
            market.setUnindexedProperty("lastTime", exec.getCreated());
        }
        entity.setProperty("archive", Boolean.FALSE);
        entity.setUnindexedProperty("created", exec.getCreated());
        entity.setUnindexedProperty("modified", exec.getCreated());
        updateMaxExecId(market, exec.getId());
        return entity;
    }

    private final Entity applyExec(Entity order, Exec exec) {
        order.setProperty("state", exec.getState().name());
        order.setUnindexedProperty("lots", exec.getLots());
        order.setUnindexedProperty("resd", exec.getResd());
        order.setUnindexedProperty("exec", exec.getExec());
        order.setUnindexedProperty("lastTicks", exec.getLastTicks());
        order.setUnindexedProperty("lastLots", exec.getLastLots());
        order.setUnindexedProperty("modified", exec.getCreated());
        return order;
    }

    private final Entity getGroup(Transaction txn, String name) {
        // Lazily for now, but we may want to explicitly create markets in the future.
        final Key key = KeyFactory.createKey(GROUP_KIND, name);
        Entity entity;
        try {
            entity = datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            entity = new Entity(key);
            datastore.put(txn, entity);
        }
        return entity;
    }

    private final Entity getMarket(Transaction txn, long contrId, int settlDay) {
        // Lazily for now, but we may want to explicitly create markets in the future.
        final Key key = KeyFactory.createKey(MARKET_KIND, Market.composeId(contrId, settlDay));
        Entity entity;
        try {
            entity = datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            entity = new Entity(key);
            entity.setUnindexedProperty("contrId", contrId);
            entity.setUnindexedProperty("settlDay", Integer.valueOf(settlDay));
            entity.setUnindexedProperty("expiryDay", Integer.valueOf(settlDay));
            entity.setUnindexedProperty("lastTicks", Long.valueOf(0L));
            entity.setUnindexedProperty("lastLots", Long.valueOf(0L));
            entity.setUnindexedProperty("lastTime", Long.valueOf(0L));
            entity.setUnindexedProperty("maxOrderId", Long.valueOf(0L));
            entity.setUnindexedProperty("maxExecId", Long.valueOf(0L));
            datastore.put(txn, entity);
        }
        return entity;
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
        final Query q = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            cb.call(entity);
        }
    }

    @Override
    public final void insertExec(long contrId, int settlDay, Exec exec) throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, contrId, settlDay);
            if (exec.getState() == State.NEW) {
                datastore.put(newOrder(market, exec));
            } else {
                datastore.put(applyExec(getOrder(txn, market.getKey(), exec.getOrderId()), exec));
            }
            datastore.put(newExec(market, exec));
            datastore.put(market);
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
    public final void insertExecList(long contrId, int settlDay, Exec first)
            throws NotFoundException {
        // N.B. the approach I used previously on a traditional RDMS was quite different, in that
        // order revisions were managed as triggers on the exec table.
        final Map<Long, Entity> orders = new HashMap<>();
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, contrId, settlDay);
            for (SlNode node = first; node != null; node = node.slNext()) {
                final Exec exec = (Exec) node;
                if (exec.getState() == State.NEW) {
                    // Defer actual datastore put.
                    orders.put(first.getOrderId(), newOrder(market, exec));
                } else {
                    final long id = exec.getOrderId();
                    // This exec may apply to a cached order.
                    Entity order = orders.get(id);
                    if (order == null) {
                        // Otherwise fetch the order from the datastore.
                        order = getOrder(txn, market.getKey(), id);
                        orders.put(id, order);
                    }
                    applyExec(order, exec);
                }
                datastore.put(newExec(market, exec));
            }
            datastore.put(orders.values());
            datastore.put(market);
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
    public final long allocTraderId() {
        final KeyRange range = datastore.allocateIds(TRADER_KIND, 1L);
        return range.getStart().getId();
    }

    @Override
    public final void insertTrader(Trader trader) {
        final Transaction txn = datastore.beginTransaction();
        try {
            // Trader entities have common ancestor for strong consistency.
            final Entity group = getGroup(txn, TRADER_KIND);
            final Entity entity = newTrader(group, trader);
            // Unique indexes.
            final Entity mnemIdx = new Entity(TRADER_MNEM_KIND, trader.getMnem(), group.getKey());
            final Entity emailIdx = new Entity(TRADER_EMAIL_KIND, trader.getEmail(), group.getKey());
            datastore.put(entity);
            datastore.put(mnemIdx);
            datastore.put(emailIdx);
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
    public final void insertMarket(long contrId, int settlDay, int expiryDay) {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = new Entity(MARKET_KIND, Market.composeId(contrId, settlDay));
            entity.setUnindexedProperty("contrId", contrId);
            entity.setUnindexedProperty("settlDay", Integer.valueOf(settlDay));
            entity.setUnindexedProperty("expiryDay", Integer.valueOf(expiryDay));
            entity.setUnindexedProperty("lastTicks", Long.valueOf(0L));
            entity.setUnindexedProperty("lastLots", Long.valueOf(0L));
            entity.setUnindexedProperty("lastTime", Long.valueOf(0L));
            entity.setUnindexedProperty("maxOrderId", Long.valueOf(0L));
            entity.setUnindexedProperty("maxExecId", Long.valueOf(0L));
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
    public final void archiveOrder(long contrId, int settlDay, long id, long modified)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, contrId, settlDay);
            final Entity entity = getOrder(txn, market.getKey(), id);
            entity.setProperty("archive", Boolean.TRUE);
            entity.setUnindexedProperty("modified", modified);
            datastore.put(entity);
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
    public final void archiveTrade(long contrId, int settlDay, long id, long modified)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, contrId, settlDay);
            final Entity entity = getExec(txn, market.getKey(), id);
            entity.setProperty("archive", Boolean.TRUE);
            entity.setUnindexedProperty("modified", modified);
            datastore.put(entity);
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
    public final void selectAsset(UnaryCallback<Asset> cb) {
        // TODO: migrate to datastore.
        MockAsset.selectAsset(cb);
    }

    @Override
    public final void selectContr(UnaryCallback<Contr> cb) {
        // TODO: migrate to datastore.
        MockContr.selectContr(cb);
    }

    @Override
    public final void selectTrader(UnaryCallback<Trader> cb) {
        final Key parent = KeyFactory.createKey(GROUP_KIND, TRADER_KIND);
        final Query q = new Query(TRADER_KIND, parent);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            final long id = entity.getKey().getId();
            final String mnem = (String) entity.getProperty("mnem");
            final String display = (String) entity.getProperty("display");
            final String email = (String) entity.getProperty("email");
            final Trader trader = new Trader(id, mnem, display, email);
            cb.call(trader);
        }
    }

    @Override
    public final void selectMarket(final UnaryCallback<Market> cb) {
        final Query q = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            final Identifiable contr = newId((Long) entity.getProperty("contrId"));
            final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
            final int expiryDay = ((Long) entity.getProperty("expiryDay")).intValue();
            final long lastTicks = (Long) entity.getProperty("lastTicks");
            final long lastLots = (Long) entity.getProperty("lastLots");
            final long lastTime = (Long) entity.getProperty("lastTime");
            final long maxOrderId = (Long) entity.getProperty("maxOrderId");
            final long maxExecId = (Long) entity.getProperty("maxExecId");
            final Market market = new Market(contr, settlDay, expiryDay, lastTicks, lastLots,
                    lastTime, maxOrderId, maxExecId);
            cb.call(market);
        }
    }

    @Override
    public final void selectOrder(final UnaryCallback<Order> cb) {
        final Filter filter = new FilterPredicate("archive", FilterOperator.EQUAL, Boolean.FALSE);
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query q = new Query(ORDER_KIND, arg.getKey()).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(q);
                for (final Entity entity : pq.asIterable()) {
                    final long id = entity.getKey().getId();
                    final Identifiable trader = newId((Long) entity.getProperty("traderId"));
                    final Identifiable contr = newId((Long) entity.getProperty("contrId"));
                    final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
                    final String ref = (String) entity.getProperty("ref");
                    final State state = State.valueOf((String) entity.getProperty("state"));
                    final Action action = Action.valueOf((String) entity.getProperty("action"));
                    final long ticks = (Long) entity.getProperty("ticks");
                    final long lots = (Long) entity.getProperty("lots");
                    final long resd = (Long) entity.getProperty("resd");
                    final long exec = (Long) entity.getProperty("exec");
                    final long lastTicks = (Long) entity.getProperty("lastTicks");
                    final long lastLots = (Long) entity.getProperty("lastLots");
                    final long minLots = (Long) entity.getProperty("minLots");
                    final long created = (Long) entity.getProperty("created");
                    final long modified = (Long) entity.getProperty("modified");
                    final Order order = new Order(id, trader, contr, settlDay, ref, state, action,
                            ticks, lots, resd, exec, lastTicks, lastLots, minLots, created,
                            modified);
                    cb.call(order);
                }
            }
        });
    }

    @Override
    public final void selectTrade(final UnaryCallback<Exec> cb) {
        final Filter stateFilter = new FilterPredicate("state", FilterOperator.EQUAL,
                State.TRADE.name());
        final Filter archiveFilter = new FilterPredicate("archive", FilterOperator.EQUAL,
                Boolean.FALSE);
        final Filter filter = CompositeFilterOperator.and(stateFilter, archiveFilter);
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query q = new Query(EXEC_KIND, arg.getKey()).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(q);
                for (final Entity entity : pq.asIterable()) {
                    final long id = entity.getKey().getId();
                    final long orderId = (Long) entity.getProperty("orderId");
                    final Identifiable trader = newId((Long) entity.getProperty("traderId"));
                    final Identifiable contr = newId((Long) entity.getProperty("contrId"));
                    final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
                    final String ref = (String) entity.getProperty("ref");
                    final State state = State.valueOf((String) entity.getProperty("state"));
                    final Action action = Action.valueOf((String) entity.getProperty("action"));
                    final long ticks = (Long) entity.getProperty("ticks");
                    final long lots = (Long) entity.getProperty("lots");
                    final long resd = (Long) entity.getProperty("resd");
                    final long exec = (Long) entity.getProperty("exec");
                    final long lastTicks = (Long) entity.getProperty("lastTicks");
                    final long lastLots = (Long) entity.getProperty("lastLots");
                    final long minLots = (Long) entity.getProperty("minLots");
                    long matchId;
                    Role role;
                    Identifiable cpty;
                    if (state == State.TRADE) {
                        matchId = (Long) entity.getProperty("matchId");
                        role = Role.valueOf((String) entity.getProperty("role"));
                        cpty = newId((Long) entity.getProperty("cptyId"));
                    } else {
                        matchId = 0;
                        role = null;
                        cpty = null;
                    }
                    final long created = (Long) entity.getProperty("created");
                    final Exec trade = new Exec(id, orderId, trader, contr, settlDay, ref, state,
                            action, ticks, lots, resd, exec, lastTicks, lastLots, minLots, matchId,
                            role, cpty, created);
                    cb.call(trade);
                }
            }
        });
    }

    @Override
    public final void selectPosn(final UnaryCallback<Posn> cb) {
        final Map<Long, Posn> m = new HashMap<>();
        final Filter filter = new FilterPredicate("state", FilterOperator.EQUAL, State.TRADE.name());
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query q = new Query(EXEC_KIND).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(q);
                for (final Entity entity : pq.asIterable()) {
                    final long traderId = (Long) entity.getProperty("traderId");
                    final long contrId = (Long) entity.getProperty("contrId");
                    final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
                    final Long posnId = Long.valueOf(Posn.composeId(contrId, settlDay, traderId));
                    // Lazy position.
                    Posn posn = m.get(posnId);
                    if (posn == null) {
                        posn = new Posn(newId(traderId), newId(contrId), settlDay);
                        m.put(posnId, posn);
                    }
                    final Action action = Action.valueOf((String) entity.getProperty("action"));
                    final long lastTicks = (Long) entity.getProperty("lastTicks");
                    final long lastLots = (Long) entity.getProperty("lastLots");
                    posn.applyTrade(action, lastTicks, lastLots);
                }
            }
        });
        for (final Posn posn : m.values()) {
            cb.call(posn);
        }
    }
}

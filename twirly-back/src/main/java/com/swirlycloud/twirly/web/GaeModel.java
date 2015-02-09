/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.MnemUtil.newMnem;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

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
import com.swirlycloud.twirly.app.Model;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
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
import com.swirlycloud.twirly.mock.MockAsset;
import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Memorable;

public final class GaeModel implements Model {

    @SuppressWarnings("unused")
    private static final String ASSET_KIND = "Asset";
    @SuppressWarnings("unused")
    private static final String CONTR_KIND = "Contr";
    private static final String TRADER_KIND = "Trader";
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

    private final Entity newTrader(Trader trader) {
        final Entity entity = new Entity(TRADER_KIND, trader.getMnem());
        entity.setUnindexedProperty("display", trader.getDisplay());
        entity.setProperty("email", trader.getEmail());
        return entity;
    }

    private final Entity newMarket(Market market) {
        final Entity entity = new Entity(MARKET_KIND, market.getMnem());
        entity.setUnindexedProperty("display", market.getDisplay());
        entity.setUnindexedProperty("contr", market.getContr());
        entity.setUnindexedProperty("settlDay", Integer.valueOf(market.getSettlDay()));
        entity.setUnindexedProperty("expiryDay", Integer.valueOf(market.getExpiryDay()));
        entity.setUnindexedProperty("lastTicks", Long.valueOf(market.getLastTicks()));
        entity.setUnindexedProperty("lastLots", Long.valueOf(market.getLastLots()));
        entity.setUnindexedProperty("lastTime", Long.valueOf(market.getLastTime()));
        entity.setUnindexedProperty("maxOrderId", Long.valueOf(market.getMaxOrderId()));
        entity.setUnindexedProperty("maxExecId", Long.valueOf(market.getMaxExecId()));
        return entity;
    }

    private final Entity newOrder(Entity market, Exec exec) {
        final Entity entity = new Entity(ORDER_KIND, exec.getOrderId(), market.getKey());
        entity.setUnindexedProperty("trader", exec.getTrader());
        entity.setUnindexedProperty("market", exec.getMarket());
        entity.setUnindexedProperty("contr", exec.getContr());
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
        entity.setUnindexedProperty("trader", exec.getTrader());
        entity.setUnindexedProperty("market", exec.getMarket());
        entity.setUnindexedProperty("contr", exec.getContr());
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
            entity.setUnindexedProperty("cpty", exec.getCpty());
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

    private final Entity getMarket(Transaction txn, String market) throws NotFoundException {
        final Key key = KeyFactory.createKey(MARKET_KIND, market);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new NotFoundException(String.format("market '%s' does not exist in datastore",
                    market));
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
        final Query q = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            cb.call(entity);
        }
    }

    @Override
    public final void insertExec(Exec exec) throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, exec.getMarket());
            if (exec.getState() == State.NEW) {
                datastore.put(txn, newOrder(market, exec));
            } else {
                datastore.put(txn, applyExec(getOrder(txn, market.getKey(), exec.getOrderId()), exec));
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
        // N.B. the approach I used previously on a traditional RDMS was quite different, in that
        // order revisions were managed as triggers on the exec table.
        final Map<Long, Entity> orders = new HashMap<>();
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, marketMnem);
            for (SlNode node = first; node != null; node = node.slNext()) {
                final Exec exec = (Exec) node;
                if (exec.getState() == State.NEW) {
                    // Defer actual datastore put.
                    orders.put(exec.getOrderId(), newOrder(market, exec));
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
                datastore.put(txn, newExec(market, exec));
            }
            datastore.put(txn, orders.values());
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
    public final void insertTrader(Trader trader) {
        final Transaction txn = datastore.beginTransaction();
        try {
            // Trader entities have common ancestor for strong consistency.
            final Entity entity = newTrader(trader);
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
    public final void insertMarket(Market market) {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = newMarket(market);
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
        final Query q = new Query(TRADER_KIND);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            final String mnem = entity.getKey().getName();
            final String display = (String) entity.getProperty("display");
            final String email = (String) entity.getProperty("email");
            final Trader trader = new Trader(mnem, display, email);
            cb.call(trader);
        }
    }

    @Override
    public final void selectMarket(final UnaryCallback<Market> cb) {
        final Query q = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            final String mnem = entity.getKey().getName();
            final String display = (String) entity.getProperty("display");
            final Memorable contr = newMnem((String) entity.getProperty("contr"));
            final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
            final int expiryDay = ((Long) entity.getProperty("expiryDay")).intValue();
            final long lastTicks = (Long) entity.getProperty("lastTicks");
            final long lastLots = (Long) entity.getProperty("lastLots");
            final long lastTime = (Long) entity.getProperty("lastTime");
            final long maxOrderId = (Long) entity.getProperty("maxOrderId");
            final long maxExecId = (Long) entity.getProperty("maxExecId");
            final Market market = new Market(mnem, display, contr, settlDay, expiryDay, lastTicks,
                    lastLots, lastTime, maxOrderId, maxExecId);
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
                    final String trader = (String) entity.getProperty("trader");
                    final String market = (String) entity.getProperty("market");
                    final String contr = (String) entity.getProperty("contr");
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
                    final Order order = new Order(id, trader, market, contr, settlDay, ref, state,
                            action, ticks, lots, resd, exec, lastTicks, lastLots, minLots, created,
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
                    final String trader = (String) entity.getProperty("trader");
                    final String market = (String) entity.getProperty("market");
                    final String contr = (String) entity.getProperty("contr");
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
                    String cpty;
                    if (state == State.TRADE) {
                        matchId = (Long) entity.getProperty("matchId");
                        role = Role.valueOf((String) entity.getProperty("role"));
                        cpty = (String) entity.getProperty("cpty");
                    } else {
                        matchId = 0;
                        role = null;
                        cpty = null;
                    }
                    final long created = (Long) entity.getProperty("created");
                    final Exec trade = new Exec(id, orderId, trader, market, contr, settlDay, ref,
                            state, action, ticks, lots, resd, exec, lastTicks, lastLots, minLots,
                            matchId, role, cpty, created);
                    cb.call(trade);
                }
            }
        });
    }

    @Override
    public final void selectPosn(final UnaryCallback<Posn> cb) {
        final PosnTree posns = new PosnTree();
        final Filter filter = new FilterPredicate("state", FilterOperator.EQUAL, State.TRADE.name());
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query q = new Query(EXEC_KIND).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(q);
                for (final Entity entity : pq.asIterable()) {
                    final String trader = (String) entity.getProperty("trader");
                    final String market = (String) entity.getProperty("market");
                    final String contr = (String) entity.getProperty("contr");
                    final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
                    // Lazy position.
                    Posn posn = (Posn) posns.pfind(trader, market);
                    if (posn == null || !posn.getTrader().equals(trader)
                            || !posn.getMarket().equals(market)) {
                        final RbNode parent = posn;
                        posn = new Posn(trader, market, contr, settlDay);
                        posns.pinsert(posn, parent);
                    }
                    final Action action = Action.valueOf((String) entity.getProperty("action"));
                    final long lastTicks = (Long) entity.getProperty("lastTicks");
                    final long lastLots = (Long) entity.getProperty("lastLots");
                    posn.applyTrade(action, lastTicks, lastLots);
                }
            }
        });
        for (;;) {
            final Posn posn = (Posn) posns.getRoot();
            if (posn == null) {
                break;
            }
            posns.remove(posn);
            cb.call(posn);
        }
    }
}

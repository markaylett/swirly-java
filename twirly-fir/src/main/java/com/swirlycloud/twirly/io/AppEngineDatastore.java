/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import static com.swirlycloud.twirly.node.SlUtil.popNext;
import static com.swirlycloud.twirly.util.NullUtil.nullIfZero;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.SlNode;

public final class AppEngineDatastore extends AppEngineModel implements Datastore {

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
        entity.setUnindexedProperty("side", exec.getSide().name());
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
        entity.setUnindexedProperty("side", exec.getSide().name());
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

    public AppEngineDatastore(Factory factory) {
        super(factory);
    }

    @Override
    public final void close() {
        super.close();
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
}

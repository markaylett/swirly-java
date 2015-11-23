/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import static com.swirlycloud.twirly.node.JslUtil.popNext;
import static com.swirlycloud.twirly.util.NullUtil.nullIfZero;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.swirlycloud.twirly.domain.MarketId;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.entity.Exec;
import com.swirlycloud.twirly.entity.Quote;
import com.swirlycloud.twirly.exception.MarketNotFoundException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.OrderNotFoundException;
import com.swirlycloud.twirly.exception.TraderNotFoundException;
import com.swirlycloud.twirly.node.JslNode;

public final class AppEngineDatastore extends AppEngineModel implements Datastore {

    private static void updateMaxOrderId(Entity market, long id) {
        Long maxOrderId = (Long) market.getProperty("maxOrderId");
        if (maxOrderId.longValue() < id) {
            maxOrderId = Long.valueOf(id);
            market.setUnindexedProperty("maxOrderId", maxOrderId);
        }
    }

    private static void updateMaxExecId(Entity market, long id) {
        Long maxExecId = (Long) market.getProperty("maxExecId");
        if (maxExecId.longValue() < id) {
            maxExecId = Long.valueOf(id);
            market.setUnindexedProperty("maxExecId", maxExecId);
        }
    }

    private static void updateMaxQuoteId(Entity market, long id) {
        Long maxQuoteId = (Long) market.getProperty("maxQuoteId");
        if (maxQuoteId.longValue() < id) {
            maxQuoteId = Long.valueOf(id);
            market.setUnindexedProperty("maxQuoteId", maxQuoteId);
        }
    }

    private static Entity newMarket(String mnem, String display, String contr, int settlDay,
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
        entity.setUnindexedProperty("maxQuoteId", zero);
        return entity;
    }

    private static Entity newTrader(String mnem, String display, String email) {
        final Entity entity = new Entity(TRADER_KIND, mnem);
        entity.setUnindexedProperty("display", display);
        entity.setProperty("email", email);
        return entity;
    }

    private static Entity newOrder(Entity market, Exec exec) {
        final Entity entity = new Entity(ORDER_KIND, exec.getOrderId(), market.getKey());
        entity.setProperty("trader", exec.getTrader());
        entity.setUnindexedProperty("market", exec.getMarket());
        entity.setUnindexedProperty("contr", exec.getContr());
        entity.setUnindexedProperty("settlDay", nullIfZero(exec.getSettlDay()));
        entity.setUnindexedProperty("ref", exec.getRef());
        entity.setUnindexedProperty("quoteId", nullIfZero(exec.getQuoteId()));
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
        entity.setProperty("pecan", exec.getState() == State.PECAN);
        entity.setUnindexedProperty("created", exec.getCreated());
        entity.setUnindexedProperty("modified", exec.getCreated());
        updateMaxOrderId(market, exec.getOrderId());
        return entity;
    }

    private static Entity newExec(Entity market, Exec exec) {
        final Entity entity = new Entity(EXEC_KIND, exec.getId(), market.getKey());
        entity.setProperty("trader", exec.getTrader());
        entity.setUnindexedProperty("market", exec.getMarket());
        entity.setUnindexedProperty("contr", exec.getContr());
        entity.setUnindexedProperty("settlDay", nullIfZero(exec.getSettlDay()));
        entity.setUnindexedProperty("ref", exec.getRef());
        entity.setUnindexedProperty("orderId", nullIfZero(exec.getOrderId()));
        entity.setUnindexedProperty("quoteId", nullIfZero(exec.getQuoteId()));
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

    private static Entity applyExec(Entity order, Exec exec) {
        order.setProperty("state", exec.getState().name());
        order.setUnindexedProperty("lots", exec.getLots());
        order.setProperty("resd", exec.getResd());
        order.setUnindexedProperty("exec", exec.getExec());
        order.setUnindexedProperty("cost", exec.getCost());
        if (exec.getLastLots() > 0) {
            order.setUnindexedProperty("lastTicks", exec.getLastTicks());
            order.setUnindexedProperty("lastLots", exec.getLastLots());
        }
        if (exec.getState() == State.PECAN) {
            order.setProperty("pecan", true);
        }
        order.setUnindexedProperty("modified", exec.getCreated());
        return order;
    }

    private final Entity getMarket(Transaction txn, String market) throws NotFoundException {
        final Key key = KeyFactory.createKey(MARKET_KIND, market);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new MarketNotFoundException(
                    String.format("market '%s' does not exist in datastore", market));
        }
    }

    private final Entity getTrader(Transaction txn, String trader) throws NotFoundException {
        final Key key = KeyFactory.createKey(TRADER_KIND, trader);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new TraderNotFoundException(
                    String.format("trader '%s' does not exist in datastore", trader));
        }
    }

    private final Entity getOrder(Transaction txn, Key parent, long id) throws NotFoundException {
        final Key key = KeyFactory.createKey(parent, ORDER_KIND, id);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new OrderNotFoundException(
                    String.format("order '%d' does not exist in datastore", id));
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

    @Override
    public final void close() {
        super.close();
    }

    @Override
    public final void createMarket(@NonNull String mnem, @Nullable String display,
            @NonNull String contr, int settlDay, int expiryDay, int state) {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = newMarket(mnem, display, contr, settlDay, expiryDay, state);
            datastore.put(txn, entity);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void updateMarket(@NonNull String mnem, @Nullable String display, int state)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = getMarket(txn, mnem);
            entity.setUnindexedProperty("display", display);
            entity.setUnindexedProperty("state", state);
            datastore.put(txn, entity);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void createTrader(@NonNull String mnem, @Nullable String display,
            @NonNull String email) {
        final Transaction txn = datastore.beginTransaction();
        try {
            // Trader entities have common ancestor for strong consistency.
            final Entity entity = newTrader(mnem, display, email);
            datastore.put(txn, entity);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void updateTrader(@NonNull String mnem, @Nullable String display)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity entity = getTrader(txn, mnem);
            entity.setUnindexedProperty("display", display);
            datastore.put(txn, entity);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void createExec(@NonNull Exec exec) throws NotFoundException {
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
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void createExecList(@NonNull String marketMnem, @NonNull JslNode first)
            throws NotFoundException {

        if (first.jslNext() == null) {
            // Singleton list.
            createExec((Exec) first);
            return;
        }

        JslNode node = first;
        try {
            // N.B. the approach I used previously on a traditional RDMS was quite different, in
            // that order revisions were managed as triggers on the exec table.
            final Map<Long, Entity> orders = new HashMap<>();
            final Transaction txn = datastore.beginTransaction();
            try {
                final Entity market = getMarket(txn, marketMnem);
                do {
                    final Exec exec = (Exec) node;
                    node = popNext(node);

                    assert marketMnem.equals(exec.getMarket());
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
                } while (node != null);

                if (!orders.isEmpty()) {
                    datastore.put(txn, orders.values());
                }
                datastore.put(txn, market);
                txn.commit();
            } catch (final ConcurrentModificationException e) {
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
    public final void createExecList(@NonNull JslNode first) throws NotFoundException {

        if (first.jslNext() == null) {
            // Singleton list.
            createExec((Exec) first);
            return;
        }

        // Partition nodes by market.

        final HashMap<String, JslNode> map;
        JslNode node = first;
        try {
            map = new HashMap<>();
            do {
                final Exec exec = (Exec) node;
                node = popNext(node);

                final String market = exec.getMarket();
                exec.setJslNext(map.get(market));
                map.put(market, exec);
            } while (node != null);
        } finally {
            // Clear nodes to ensure no unwanted retention.
            while (node != null) {
                node = popNext(node);
            }
        }

        // Execution transaction for each market.

        for (final Entry<String, JslNode> entry : map.entrySet()) {
            final String key = entry.getKey();
            final JslNode value = entry.getValue();
            assert key != null;
            assert value != null;
            createExecList(key, value);
        }
    }

    @Override
    public final void createQuote(@NonNull Quote quote) throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, quote.getMarket());
            updateMaxQuoteId(market, quote.getId());
            datastore.put(txn, market);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void archiveOrder(@NonNull String marketMnem, long id, long modified)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, marketMnem);
            final Entity entity = getOrder(txn, market.getKey(), id);
            entity.setProperty("archive", Boolean.TRUE);
            entity.setUnindexedProperty("modified", modified);
            datastore.put(txn, entity);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void archiveOrderList(@NonNull String marketMnem, @NonNull JslNode first,
            long modified) throws NotFoundException {

        if (first.jslNext() == null) {
            // Singleton list.
            final MarketId mid = (MarketId) first;
            archiveOrder(marketMnem, mid.getId(), modified);
            return;
        }

        JslNode node = first;
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, marketMnem);
            do {
                final MarketId mid = (MarketId) node;
                node = node.jslNext();

                assert marketMnem.equals(mid.getMarket());
                final Entity entity = getOrder(txn, market.getKey(), mid.getId());
                entity.setProperty("archive", Boolean.TRUE);
                entity.setUnindexedProperty("modified", modified);
                datastore.put(txn, entity);
            } while (node != null);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void archiveOrderList(@NonNull JslNode first, long modified)
            throws NotFoundException {

        if (first.jslNext() == null) {
            // Singleton list.
            final MarketId mid = (MarketId) first;
            archiveOrder(mid.getMarket(), mid.getId(), modified);
            return;
        }

        // Partition nodes by market.

        final HashMap<String, JslNode> map = new HashMap<>();
        JslNode node = first;
        do {
            final MarketId mid = (MarketId) node;
            node = node.jslNext();

            final String market = mid.getMarket();
            mid.setJslNext(map.get(market));
            map.put(market, mid);
        } while (node != null);

        // Execution transaction for each market.

        for (final Entry<String, JslNode> entry : map.entrySet()) {
            final String key = entry.getKey();
            final JslNode value = entry.getValue();
            assert key != null;
            assert value != null;
            archiveOrderList(key, value, modified);
        }
    }

    @Override
    public final void archiveTrade(@NonNull String marketMnem, long id, long modified)
            throws NotFoundException {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, marketMnem);
            final Entity entity = getExec(txn, market.getKey(), id);
            entity.setProperty("archive", Boolean.TRUE);
            entity.setUnindexedProperty("modified", modified);
            datastore.put(txn, entity);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void archiveTradeList(@NonNull String marketMnem, @NonNull JslNode first,
            long modified) throws NotFoundException {

        if (first.jslNext() == null) {
            // Singleton list.
            final MarketId mid = (MarketId) first;
            archiveTrade(marketMnem, mid.getId(), modified);
            return;
        }

        JslNode node = first;
        final Transaction txn = datastore.beginTransaction();
        try {
            final Entity market = getMarket(txn, marketMnem);
            do {
                final MarketId mid = (MarketId) node;
                node = node.jslNext();

                assert marketMnem.equals(mid.getMarket());
                final Entity entity = getExec(txn, market.getKey(), mid.getId());
                entity.setProperty("archive", Boolean.TRUE);
                entity.setUnindexedProperty("modified", modified);
                datastore.put(txn, entity);
            } while (node != null);
            txn.commit();
        } catch (final ConcurrentModificationException e) {
            // FIXME: implement retry logic.
            throw e;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void archiveTradeList(@NonNull JslNode first, long modified)
            throws NotFoundException {

        if (first.jslNext() == null) {
            // Singleton list.
            final MarketId mid = (MarketId) first;
            archiveTrade(mid.getMarket(), mid.getId(), modified);
            return;
        }

        // Partition nodes by market.

        final HashMap<String, JslNode> map = new HashMap<>();
        JslNode node = first;
        do {
            final MarketId mid = (MarketId) node;
            node = node.jslNext();

            final String market = mid.getMarket();
            mid.setJslNext(map.get(market));
            map.put(market, mid);
        } while (node != null);

        // Execution transaction for each market.

        for (final Entry<String, JslNode> entry : map.entrySet()) {
            final String key = entry.getKey();
            final JslNode value = entry.getValue();
            assert key != null;
            assert value != null;
            archiveTradeList(key, value, modified);
        }
    }
}

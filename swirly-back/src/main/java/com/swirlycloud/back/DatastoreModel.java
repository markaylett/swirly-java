/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.util.AshUtil.newId;

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
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Role;
import com.swirlycloud.domain.State;
import com.swirlycloud.domain.User;
import com.swirlycloud.engine.Market;
import com.swirlycloud.engine.Model;
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
    private static final String USER_KIND = "User";
    private static final String USER_MNEM_KIND = "UserMnem";
    private static final String USER_EMAIL_KIND = "UserEmail";
    private static final String MARKET_KIND = "Market";
    private static final String ORDER_KIND = "Order";
    private static final String EXEC_KIND = "Exec";

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private final Entity newUser(Key parent, User user) {
        final Entity entity = new Entity(USER_KIND, user.getId(), parent);
        entity.setUnindexedProperty("mnem", user.getMnem());
        entity.setUnindexedProperty("display", user.getDisplay());
        entity.setUnindexedProperty("email", user.getEmail());
        return entity;
    }

    private final Entity newOrder(Key parent, Exec exec) {
        final Entity entity = new Entity(ORDER_KIND, exec.getOrderId(), parent);
        entity.setUnindexedProperty("userId", exec.getUserId());
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
        entity.setUnindexedProperty("created", exec.getCreated());
        entity.setUnindexedProperty("modified", exec.getCreated());
        return entity;
    }

    private final Entity newExec(Key parent, Exec exec) {
        final Entity entity = new Entity(EXEC_KIND, exec.getId(), parent);
        entity.setUnindexedProperty("orderId", exec.getOrderId());
        entity.setUnindexedProperty("userId", exec.getUserId());
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
        }
        entity.setProperty("confirmed", Boolean.FALSE);
        entity.setUnindexedProperty("created", exec.getCreated());
        entity.setUnindexedProperty("modified", exec.getCreated());
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
        final Key key = KeyFactory
                .createKey(MARKET_KIND, Market.composeId(contrId, settlDay));
        Entity entity;
        try {
            entity = datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            entity = new Entity(key);
            entity.setUnindexedProperty("contrId", contrId);
            entity.setUnindexedProperty("settlDay", Integer.valueOf(settlDay));
            datastore.put(txn, entity);
        }
        return entity;
    }

    private final Entity getOrder(Transaction txn, Key parent, long id) {
        final Key key = KeyFactory.createKey(parent, ORDER_KIND, id);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final Entity getExec(Transaction txn, Key parent, long id) {
        final Key key = KeyFactory.createKey(parent, EXEC_KIND, id);
        try {
            return datastore.get(txn, key);
        } catch (final EntityNotFoundException e) {
            throw new IllegalArgumentException(e);
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
    public final long allocUserIds(long num) {
        final KeyRange range = datastore.allocateIds(USER_KIND, num);
        return range.getStart().getId();
    }

    @Override
    public final long allocOrderIds(long num) {
        final KeyRange range = datastore.allocateIds(ORDER_KIND, num);
        return range.getStart().getId();
    }

    @Override
    public final long allocExecIds(long num) {
        final KeyRange range = datastore.allocateIds(EXEC_KIND, num);
        return range.getStart().getId();
    }

    @Override
    public final void insertUser(User user) {
        final Transaction txn = datastore.beginTransaction();
        try {
            // User entities have common ancestor for strong consistency.
            final Key parent = getGroup(txn, USER_KIND).getKey();
            final Entity entity = newUser(parent, user);
            // Unique indexes.
            final Entity mnemIdx = new Entity(USER_MNEM_KIND, user.getMnem(), parent);
            final Entity emailIdx = new Entity(USER_EMAIL_KIND, user.getEmail(), parent);
            datastore.put(entity);
            datastore.put(mnemIdx);
            datastore.put(emailIdx);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void insertExecList(long contrId, int settlDay, Exec first) {
        // N.B. the approach I used previously on a traditional RDMS was quite different, in that
        // order revisions were managed as triggers on the exec table.
        final Map<Long, Entity> orders = new HashMap<>();
        final Transaction txn = datastore.beginTransaction();
        try {
            final Key parent = getMarket(txn, contrId, settlDay).getKey();
            for (SlNode node = first; node != null; node = node.slNext()) {
                final Exec exec = (Exec) node;
                if (exec.getState() == State.NEW) {
                    // Defer actual datastore put.
                    orders.put(first.getOrderId(), newOrder(parent, exec));
                } else {
                    final long id = exec.getOrderId();
                    // This exec may apply to a cached order.
                    Entity order = orders.get(id);
                    if (order == null) {
                        // Otherwise fetch the order from the datastore.
                        order = getOrder(txn, parent, id);
                        orders.put(id, order);
                    }
                    applyExec(order, exec);
                }
                datastore.put(newExec(parent, exec));
            }
            datastore.put(orders.values());
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void insertExec(long contrId, int settlDay, Exec exec) {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Key parent = getMarket(txn, contrId, settlDay).getKey();
            if (exec.getState() == State.NEW) {
                datastore.put(newOrder(parent, exec));
            } else {
                datastore.put(applyExec(getOrder(txn, parent, exec.getOrderId()), exec));
            }
            datastore.put(newExec(parent, exec));
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void updateExec(long contrId, int settlDay, long id, long modified) {
        final Transaction txn = datastore.beginTransaction();
        try {
            final Key parent = getMarket(txn, contrId, settlDay).getKey();
            final Entity entity = getExec(txn, parent, id);
            entity.setProperty("confirmed", Boolean.TRUE);
            entity.setUnindexedProperty("modified", modified);
            datastore.put(entity);
            txn.commit();
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
    public final void selectUser(UnaryCallback<User> cb) {
        final Key parent = KeyFactory.createKey(GROUP_KIND, USER_KIND);
        final Query q = new Query(USER_KIND, parent);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            final long id = entity.getKey().getId();
            final String mnem = (String) entity.getProperty("mnem");
            final String display = (String) entity.getProperty("display");
            final String email = (String) entity.getProperty("email");
            final User user = new User(id, mnem, display, email);
            cb.call(user);
        }
    }

    @Override
    public final void selectMarket(final UnaryCallback<Market> cb) {
        final Query q = new Query(MARKET_KIND);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            final Identifiable contr = newId((Long) entity.getProperty("contrId"));
            final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
            final Market market = new Market(contr, settlDay);
            cb.call(market);
        }
    }

    @Override
    public final void selectOrder(final UnaryCallback<Order> cb) {
        final Filter filter = new FilterPredicate("resd", FilterOperator.GREATER_THAN, 0);
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query q = new Query(ORDER_KIND, arg.getKey()).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(q);
                for (final Entity entity : pq.asIterable()) {
                    final long id = entity.getKey().getId();
                    final Identifiable user = newId((Long) entity.getProperty("userId"));
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
                    final Order order = new Order(id, user, contr, settlDay, ref, state, action,
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
        final Filter confirmedFilter = new FilterPredicate("confirmed", FilterOperator.EQUAL,
                Boolean.FALSE);
        final Filter filter = CompositeFilterOperator.and(stateFilter, confirmedFilter);
        foreachMarket(new UnaryCallback<Entity>() {
            @Override
            public final void call(Entity arg) {
                final Query q = new Query(EXEC_KIND, arg.getKey()).setFilter(filter);
                final PreparedQuery pq = datastore.prepare(q);
                for (final Entity entity : pq.asIterable()) {
                    final long id = entity.getKey().getId();
                    final long orderId = (Long) entity.getProperty("orderId");
                    final Identifiable user = newId((Long) entity.getProperty("userId"));
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
                    final Exec trade = new Exec(id, orderId, user, contr, settlDay, ref, state,
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
                    final long userId = (Long) entity.getProperty("userId");
                    final long contrId = (Long) entity.getProperty("contrId");
                    final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
                    final Long posnId = Long.valueOf(Posn.composeId(contrId, settlDay, userId));
                    // Lazy position.
                    Posn posn = m.get(posnId);
                    if (posn == null) {
                        posn = new Posn(newId(userId), newId(contrId), settlDay);
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

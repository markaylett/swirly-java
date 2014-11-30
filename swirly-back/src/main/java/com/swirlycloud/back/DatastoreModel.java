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
import com.google.appengine.api.datastore.TransactionOptions;
import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Role;
import com.swirlycloud.domain.State;
import com.swirlycloud.domain.User;
import com.swirlycloud.engine.Model;
import com.swirlycloud.function.UnaryCallback;
import com.swirlycloud.mock.MockAsset;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.SlNode;

public final class DatastoreModel implements Model {

    // Cross-group transactions need to be explicitly specified.
    private static final TransactionOptions XG_OPTION = TransactionOptions.Builder.withXG(true);

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private final Entity newUser(User user) {
        final String kind = Kind.USER.camelName();
        final Entity entity = new Entity(kind, user.getId());
        entity.setUnindexedProperty("mnem", user.getMnem());
        entity.setUnindexedProperty("display", user.getDisplay());
        entity.setUnindexedProperty("email", user.getEmail());
        return entity;
    }

    private final Entity newOrder(Exec exec) {
        final String kind = Kind.ORDER.camelName();
        final Entity entity = new Entity(kind, exec.getOrderId());
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

    private final Entity newExec(Exec exec) {
        final String kind = Kind.EXEC.camelName();
        final Entity entity = new Entity(kind, exec.getId());
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

    private final Entity getOrder(long id) {
        final String kind = Kind.ORDER.camelName();
        final Key key = KeyFactory.createKey(kind, id);
        try {
            return datastore.get(key);
        } catch (final EntityNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final Entity getExec(long id) {
        final String kind = Kind.EXEC.camelName();
        final Key key = KeyFactory.createKey(kind, id);
        try {
            return datastore.get(key);
        } catch (final EntityNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public final long allocIds(Kind kind, long num) {
        final KeyRange range = datastore.allocateIds(kind.camelName(), num);
        return range.getStart().getId();
    }

    @Override
    public final void insertUser(User user) {
        final Transaction txn = datastore.beginTransaction(XG_OPTION);
        try {
            final Entity entity = newUser(user);
            final Entity mnemIdx = new Entity("UserMnem", user.getMnem());
            final Entity emailIdx = new Entity("UserEmail", user.getEmail());
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
    public final void insertExecList(Exec first) {
        // N.B. the approach I used previously on a traditional RDMS was quite different, in that
        // order revisions were managed as triggers on the exec table.
        final Map<Long, Entity> orders = new HashMap<>();
        final Transaction txn = datastore.beginTransaction(XG_OPTION);
        try {
            for (SlNode node = first; node != null; node = node.slNext()) {
                final Exec exec = (Exec) node;
                if (exec.getState() == State.NEW) {
                    // Defer actual datastore put.
                    orders.put(first.getOrderId(), newOrder(exec));
                } else {
                    final long id = exec.getOrderId();
                    // This exec may apply to a cached order.
                    Entity order = orders.get(id);
                    if (order == null) {
                        // Otherwise fetch the order from the datastore.
                        order = getOrder(id);
                        orders.put(id, order);
                    }
                    applyExec(order, exec);
                }
                datastore.put(newExec(exec));
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
    public final void insertExec(Exec exec) {
        final Transaction txn = datastore.beginTransaction(XG_OPTION);
        try {
            if (exec.getState() == State.NEW) {
                datastore.put(newOrder(exec));
            } else {
                datastore.put(applyExec(getOrder(exec.getOrderId()), exec));
            }
            datastore.put(newExec(exec));
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void updateExec(long id, long modified) {
        final Transaction txn = datastore.beginTransaction(XG_OPTION);
        try {
            final Entity entity = getExec(id);
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
        final String kind = Kind.USER.camelName();
        final Query q = new Query(kind);
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
    public final void selectOrder(UnaryCallback<Order> cb) {
        final String kind = Kind.ORDER.camelName();
        final Filter filter = new FilterPredicate("resd", FilterOperator.GREATER_THAN, 0);
        final Query q = new Query(kind).setFilter(filter);
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
            final Order order = new Order(id, user, contr, settlDay, ref, state, action, ticks,
                    lots, resd, exec, lastTicks, lastLots, minLots, created, modified);
            cb.call(order);
        }
    }

    @Override
    public final void selectTrade(UnaryCallback<Exec> cb) {
        final String kind = Kind.EXEC.camelName();
        final Filter stateFilter = new FilterPredicate("state", FilterOperator.EQUAL,
                State.TRADE.name());
        final Filter confirmedFilter = new FilterPredicate("confirmed", FilterOperator.EQUAL,
                Boolean.FALSE);
        final Filter filter = CompositeFilterOperator.and(stateFilter, confirmedFilter);
        final Query q = new Query(kind).setFilter(filter);
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
            final Exec trade = new Exec(id, orderId, user, contr, settlDay, ref, state, action,
                    ticks, lots, resd, exec, lastTicks, lastLots, minLots, matchId, role, cpty,
                    created);
            cb.call(trade);
        }
    }

    @Override
    public final void selectPosn(UnaryCallback<Posn> cb) {
        final Map<Long, Posn> m = new HashMap<>();
        final String kind = Kind.EXEC.camelName();
        final Filter filter = new FilterPredicate("state", FilterOperator.EQUAL, State.TRADE.name());
        final Query q = new Query(kind).setFilter(filter);
        final PreparedQuery pq = datastore.prepare(q);
        for (final Entity entity : pq.asIterable()) {
            final long userId = (Long) entity.getProperty("userId");
            final long contrId = (Long) entity.getProperty("contrId");
            final int settlDay = ((Long) entity.getProperty("settlDay")).intValue();
            final Long posnId = Long.valueOf(Posn.toId(userId, contrId, settlDay));
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
        for (final Posn posn : m.values()) {
            cb.call(posn);
        }
    }
}

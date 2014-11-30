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
    private static final TransactionOptions TXN_OPTIONS = TransactionOptions.Builder.withXG(true);

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private final Entity newUser(User user) {
        final String kind = Kind.USER.camelName();
        final Entity entity = new Entity(kind, user.getId());
        entity.setProperty("mnem", user.getMnem());
        entity.setProperty("display", user.getDisplay());
        entity.setProperty("email", user.getEmail());
        return entity;
    }

    private final Entity newOrder(Exec exec) {
        final String kind = Kind.ORDER.camelName();
        final Entity entity = new Entity(kind, exec.getOrderId());
        entity.setProperty("userId", exec.getUserId());
        entity.setProperty("contrId", exec.getContrId());
        entity.setProperty("settlDay", Integer.valueOf(exec.getSettlDay()));
        entity.setProperty("ref", exec.getRef());
        entity.setProperty("state", exec.getState().name());
        entity.setProperty("action", exec.getAction().name());
        entity.setProperty("ticks", exec.getTicks());
        entity.setProperty("lots", exec.getLots());
        entity.setProperty("resd", exec.getResd());
        entity.setProperty("exec", exec.getExec());
        entity.setProperty("lastTicks", exec.getLastTicks());
        entity.setProperty("lastLots", exec.getLastLots());
        entity.setProperty("minLots", exec.getMinLots());
        entity.setProperty("created", exec.getCreated());
        entity.setProperty("modified", exec.getCreated());
        return entity;
    }

    private final Entity newExec(Exec exec) {
        final String kind = Kind.EXEC.camelName();
        final Entity entity = new Entity(kind, exec.getId());
        entity.setProperty("orderId", exec.getOrderId());
        entity.setProperty("userId", exec.getUserId());
        entity.setProperty("contrId", exec.getContrId());
        entity.setProperty("settlDay", Integer.valueOf(exec.getSettlDay()));
        entity.setProperty("ref", exec.getRef());
        entity.setProperty("state", exec.getState().name());
        entity.setProperty("action", exec.getAction().name());
        entity.setProperty("ticks", exec.getTicks());
        entity.setProperty("lots", exec.getLots());
        entity.setProperty("resd", exec.getResd());
        entity.setProperty("exec", exec.getExec());
        entity.setProperty("lastTicks", exec.getLastTicks());
        entity.setProperty("lastLots", exec.getLastLots());
        entity.setProperty("minLots", exec.getMinLots());
        if (exec.getState() == State.TRADE) {
            entity.setProperty("matchId", exec.getMatchId());
            entity.setProperty("role", exec.getRole().name());
            entity.setProperty("cptyId", exec.getCptyId());
        }
        entity.setProperty("confirmed", Boolean.FALSE);
        entity.setProperty("created", exec.getCreated());
        entity.setProperty("modified", exec.getCreated());
        return entity;
    }

    private final void applyExec(Entity order, Exec exec) {
        order.setProperty("state", exec.getState().name());
        order.setProperty("lots", exec.getLots());
        order.setProperty("resd", exec.getResd());
        order.setProperty("exec", exec.getExec());
        order.setProperty("lastTicks", exec.getLastTicks());
        order.setProperty("lastLots", exec.getLastLots());
        order.setProperty("modified", exec.getCreated());
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

    private final void doInsertUser(User user) {
        final Entity entity = newUser(user);
        final Entity mnemIdx = new Entity("UserMnem", user.getMnem());
        final Entity emailIdx = new Entity("UserEmail", user.getEmail());
        datastore.put(entity);
        datastore.put(mnemIdx);
        datastore.put(emailIdx);
    }

    private final void doInsertOrder(Exec exec) {
        final Entity entity = newOrder(exec);
        datastore.put(entity);
    }

    private final void doUpdateOrder(Exec exec) {
        final Entity entity = getOrder(exec.getOrderId());
        applyExec(entity, exec);
        datastore.put(entity);
    }

    private final void doInsertExec(Exec exec) {
        final Entity entity = newExec(exec);
        datastore.put(entity);
    }

    private final void doUpdateExec(long id, long modified) {
        final Entity entity = getExec(id);
        entity.setProperty("confirmed", Boolean.TRUE);
        entity.setProperty("modified", modified);
        datastore.put(entity);
    }

    @Override
    public final long allocIds(Kind kind, long num) {
        final KeyRange range = datastore.allocateIds(kind.camelName(), num);
        return range.getStart().getId();
    }

    @Override
    public final void insertUser(User user) {
        final Transaction txn = datastore.beginTransaction(TXN_OPTIONS);
        try {
            doInsertUser(user);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void insertExecList(Exec first) {
        final Map<Long, Entity> orders = new HashMap<>();
        final Transaction txn = datastore.beginTransaction(TXN_OPTIONS);
        try {
            for (SlNode node = first; node != null; node = node.slNext()) {
                final Exec exec = (Exec) node;
                if (exec.getState() == State.NEW) {
                    orders.put(first.getOrderId(), newOrder(exec));
                } else {
                    final long id = exec.getOrderId();
                    Entity order = orders.get(id);
                    if (order == null) {
                        order = getOrder(id);
                        orders.put(id, order);
                    }
                    applyExec(order, exec);
                }
                doInsertExec(exec);
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
        final Transaction txn = datastore.beginTransaction(TXN_OPTIONS);
        try {
            if (exec.getState() == State.NEW) {
                doInsertOrder(exec);
            } else {
                doUpdateOrder(exec);
            }
            doInsertExec(exec);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @Override
    public final void updateExec(long id, long modified) {
        final Transaction txn = datastore.beginTransaction(TXN_OPTIONS);
        try {
            doUpdateExec(id, modified);
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
            final Exec trade = new Exec(id, orderId, user, contr, settlDay, ref, state, action, ticks, lots,
                    resd, exec, lastTicks, lastLots, minLots, matchId, role, cpty, created);
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

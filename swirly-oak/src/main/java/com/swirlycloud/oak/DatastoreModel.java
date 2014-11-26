/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.oak;

import static com.swirlycloud.util.AshFactory.newId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.domain.Role;
import com.swirlycloud.domain.State;
import com.swirlycloud.engine.Model;
import com.swirlycloud.mock.MockAsset;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockUser;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.SlNode;

public final class DatastoreModel implements Model {

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private final void doInsertOrder(Exec exec) {
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
        datastore.put(entity);
    }

    private final void doUpdateOrder(Exec exec) {
        final String kind = Kind.ORDER.camelName();
        final Key key = KeyFactory.createKey(kind, exec.getOrderId());
        try {
            final Entity entity = datastore.get(key);
            entity.setProperty("state", exec.getState().name());
            entity.setProperty("lots", exec.getLots());
            entity.setProperty("resd", exec.getResd());
            entity.setProperty("exec", exec.getExec());
            entity.setProperty("lastTicks", exec.getLastTicks());
            entity.setProperty("lastLots", exec.getLastLots());
            entity.setProperty("modified", exec.getCreated());
            datastore.put(entity);
        } catch (final EntityNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final void doInsertExec(Exec exec) {
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
        datastore.put(entity);
    }

    private final void doUpdateExec(long id, long modified) {
        final String kind = Kind.EXEC.camelName();
        final Key key = KeyFactory.createKey(kind, id);
        try {
            final Entity entity = datastore.get(key);
            entity.setProperty("confirmed", Boolean.TRUE);
            entity.setProperty("modified", modified);
            datastore.put(entity);
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
    public final void insertExecList(Exec first) {
        for (SlNode node = first; node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (exec.getState() == State.NEW) {
                doInsertOrder(exec);
            } else {
                doUpdateOrder(exec);
            }
            doInsertExec(exec);
        }
    }

    @Override
    public final void insertExec(Exec exec) {
        if (exec.getState() == State.NEW) {
            doInsertOrder(exec);
        } else {
            doUpdateOrder(exec);
        }
        doInsertExec(exec);
    }

    @Override
    public final void updateExec(long id, long modified) {
        doUpdateExec(id, modified);
    }

    @Override
    public final Rec getRecList(Kind kind) {
        Rec first = null;
        switch (kind) {
        case ASSET:
            first = MockAsset.newAssetList();
            break;
        case CONTR:
            first = MockContr.newContrList();
            break;
        case USER:
            first = MockUser.newUserList();
            break;
        default:
            throw new IllegalArgumentException("invalid record-type");
        }
        return first;
    }

    @Override
    public final List<Order> getOrders() {
        final List<Order> l = new ArrayList<>();
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
            l.add(order);
        }
        return l;
    }

    @Override
    public final List<Exec> getTrades() {
        final List<Exec> l = new ArrayList<>();
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
            l.add(new Exec(id, orderId, user, contr, settlDay, ref, state, action, ticks, lots,
                    resd, exec, lastTicks, lastLots, minLots, matchId, role, cpty, created));
        }
        return l;
    }

    @Override
    public final List<Posn> getPosns() {
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
        return new ArrayList<>(m.values());
    }
}

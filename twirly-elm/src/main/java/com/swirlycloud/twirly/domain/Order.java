/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.DlNode;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public final class Order extends BasicRbNode implements Jsonifiable, DlNode, SlNode, Instruct {

    private transient DlNode dlPrev;
    private transient DlNode dlNext;

    private transient SlNode slNext;

    // Internals.
    transient RbNode level;

    private final long id;
    /**
     * The executing trader.
     */
    private final String trader;
    private final String market;
    private final String contr;
    private final int settlDay;
    /**
     * Ref is optional.
     */
    @NonNull
    private final String ref;
    State state;
    private final Action action;
    private final long ticks;
    /**
     * Must be greater than zero.
     */
    long lots;
    /**
     * Must be greater than zero.
     */
    long resd;
    /**
     * Must not be greater that lots.
     */
    long exec;
    long cost;
    long lastTicks;
    long lastLots;
    /**
     * Minimum to be filled by this
     */
    private final long minLots;
    long created;
    long modified;

    public Order(long id, String trader, String market, String contr, int settlDay, String ref,
            State state, Action action, long ticks, long lots, long resd, long exec, long cost,
            long lastTicks, long lastLots, long minLots, long created, long modified) {
        assert trader != null;
        assert market != null;
        assert lots > 0 && lots >= minLots;
        this.id = id;
        this.trader = trader;
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.ref = ref != null ? ref : "";
        this.state = state;
        this.action = action;
        this.ticks = ticks;
        this.lots = lots;
        this.resd = resd;
        this.exec = exec;
        this.cost = cost;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.minLots = minLots;
        this.created = created;
        this.modified = modified;
    }

    public Order(long id, String trader, Financial fin, String ref, State state, Action action,
            long ticks, long lots, long resd, long exec, long cost, long lastTicks,
            long lastLots, long minLots, long created, long modified) {
        assert trader != null;
        assert lots > 0 && lots >= minLots;
        this.id = id;
        this.trader = trader;
        this.market = fin.getMarket();
        this.contr = fin.getContr();
        this.settlDay = fin.getSettlDay();
        this.ref = ref != null ? ref : "";
        this.state = state;
        this.action = action;
        this.ticks = ticks;
        this.lots = lots;
        this.resd = resd;
        this.exec = exec;
        this.cost = cost;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.minLots = minLots;
        this.created = created;
        this.modified = modified;
    }

    public Order(long id, String trader, String market, String contr, int settlDay, String ref,
            Action action, long ticks, long lots, long minLots, long created) {
        assert trader != null;
        assert market != null;
        assert lots > 0 && lots >= minLots;
        this.id = id;
        this.trader = trader;
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.ref = ref != null ? ref : "";
        this.state = State.NEW;
        this.action = action;
        this.ticks = ticks;
        this.lots = lots;
        this.resd = lots;
        this.exec = 0;
        this.cost = 0;
        this.lastTicks = 0;
        this.lastLots = 0;
        this.minLots = minLots;
        this.created = created;
        this.modified = created;
    }

    public Order(long id, String trader, Financial fin, String ref, Action action, long ticks,
            long lots, long minLots, long created) {
        assert trader != null;
        assert lots > 0 && lots >= minLots;
        this.id = id;
        this.trader = trader;
        this.market = fin.getMarket();
        this.contr = fin.getContr();
        this.settlDay = fin.getSettlDay();
        this.ref = ref != null ? ref : "";
        this.state = State.NEW;
        this.action = action;
        this.ticks = ticks;
        this.lots = lots;
        this.resd = lots;
        this.exec = 0;
        this.cost = 0;
        this.lastTicks = 0;
        this.lastLots = 0;
        this.minLots = minLots;
        this.created = created;
        this.modified = created;
    }

    public static Order parse(JsonParser p) throws IOException {
        long id = 0;
        String trader = null;
        String market = null;
        String contr = null;
        int settlDay = 0;
        String ref = null;
        State state = null;
        Action action = null;
        long ticks = 0;
        long lots = 0;
        long resd = 0;
        long exec = 0;
        long cost = 0;
        long lastTicks = 0;
        long lastLots = 0;
        long minLots = 0;
        long created = 0;
        long modified = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Order(id, trader, market, contr, settlDay, ref, state, action, ticks,
                        lots, resd, exec, cost, lastTicks, lastLots, minLots, created, modified);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NULL:
                if ("ref".equals(name)) {
                    ref = "";
                } else if ("lastTicks".equals(name)) {
                    lastTicks = 0;
                } else if ("lastLots".equals(name)) {
                    lastLots = 0;
                } else {
                    throw new IOException(String.format("unexpected null field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else if ("settlDate".equals(name)) {
                    settlDay = JulianDay.isoToJd(p.getInt());
                } else if ("ticks".equals(name)) {
                    ticks = p.getLong();
                } else if ("lots".equals(name)) {
                    lots = p.getLong();
                } else if ("resd".equals(name)) {
                    resd = p.getLong();
                } else if ("exec".equals(name)) {
                    exec = p.getLong();
                } else if ("cost".equals(name)) {
                    cost = p.getLong();
                } else if ("lastTicks".equals(name)) {
                    lastTicks = p.getLong();
                } else if ("lastLots".equals(name)) {
                    lastLots = p.getLong();
                } else if ("minLots".equals(name)) {
                    minLots = p.getLong();
                } else if ("created".equals(name)) {
                    created = p.getLong();
                } else if ("modified".equals(name)) {
                    modified = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("trader".equals(name)) {
                    trader = p.getString();
                } else if ("market".equals(name)) {
                    market = p.getString();
                } else if ("contr".equals(name)) {
                    contr = p.getString();
                } else if ("ref".equals(name)) {
                    ref = p.getString();
                } else if ("state".equals(name)) {
                    state = State.valueOf(p.getString());
                } else if ("action".equals(name)) {
                    action = Action.valueOf(p.getString());
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + market.hashCode();
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Order other = (Order) obj;
        if (!market.equals(other.market)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out) throws IOException {
        out.append("{\"id\":").append(String.valueOf(id));
        out.append(",\"trader\":\"").append(trader);
        out.append("\",\"market\":\"").append(market);
        out.append("\",\"contr\":\"").append(contr);
        out.append("\",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
        out.append(",\"ref\":");
        if (!ref.isEmpty()) {
            out.append('"').append(ref).append('"');
        } else {
            out.append("null");
        }
        out.append(",\"state\":\"").append(state.name());
        out.append("\",\"action\":\"").append(action.name());
        out.append("\",\"ticks\":").append(String.valueOf(ticks));
        out.append(",\"lots\":").append(String.valueOf(lots));
        out.append(",\"resd\":").append(String.valueOf(resd));
        out.append(",\"exec\":").append(String.valueOf(exec));
        out.append(",\"cost\":").append(String.valueOf(cost));
        if (lastLots != 0) {
            out.append(",\"lastTicks\":").append(String.valueOf(lastTicks));
            out.append(",\"lastLots\":").append(String.valueOf(lastLots));
        } else {
            out.append(",\"lastTicks\":null,\"lastLots\":null");
        }
        out.append(",\"minLots\":").append(String.valueOf(minLots));
        out.append(",\"created\":").append(String.valueOf(created));
        out.append(",\"modified\":").append(String.valueOf(modified));
        out.append("}");
    }

    @Override
    public final void insert(DlNode prev, DlNode next) {

        assert prev != null;
        assert next != null;

        prev.setDlNext(this);
        this.setDlPrev(prev);

        next.setDlPrev(this);
        this.setDlNext(next);
    }

    @Override
    public final void insertBefore(DlNode next) {
        assert next != null;
        insert(next.dlPrev(), next);
    }

    @Override
    public final void insertAfter(DlNode prev) {
        assert prev != null;
        insert(prev, prev.dlNext());
    }

    @Override
    public final void remove() {
        dlNext().setDlPrev(dlPrev);
        dlPrev().setDlNext(dlNext);
        setDlPrev(null);
        setDlNext(null);
    }

    @Override
    public void setDlPrev(DlNode prev) {
        this.dlPrev = prev;
    }

    @Override
    public void setDlNext(DlNode next) {
        this.dlNext = next;
    }

    @Override
    public final DlNode dlNext() {
        return this.dlNext;
    }

    @Override
    public final DlNode dlPrev() {
        return this.dlPrev;
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public final void setSlNext(SlNode next) {
        this.slNext = next;
    }

    @Override
    public final SlNode slNext() {
        return slNext;
    }

    public final void place(long now) {
        assert lots > 0 && lots >= minLots;
        state = State.NEW;
        resd = lots;
        exec = 0;
        cost = 0;
        modified = now;
    }

    public final void revise(long lots, long now) {
        assert lots > 0;
        assert lots >= exec && lots >= minLots && lots <= this.lots;
        final long delta = this.lots - lots;
        assert delta >= 0;
        state = State.REVISE;
        this.lots = lots;
        resd -= delta;
        modified = now;
    }

    public final void cancel(long now) {
        state = State.CANCEL;
        // Note that executed lots is not affected.
        resd = 0;
        modified = now;
    }

    public final void trade(long takenLots, long takenCost, long lastTicks, long lastLots, long now) {
        state = State.TRADE;
        resd -= takenLots;
        this.exec += takenLots;
        this.cost += takenCost;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        modified = now;
    }

    public final void trade(long lastTicks, long lastLots, long now) {
        trade(lastLots, lastLots * lastTicks, lastTicks, lastLots, now);
    }

    @Override
    public final long getId() {
        return id;
    }

    @Override
    public final long getOrderId() {
        return id;
    }

    @Override
    public final String getTrader() {
        return trader;
    }

    @Override
    public final String getMarket() {
        return market;
    }

    @Override
    public final String getContr() {
        return contr;
    }

    @Override
    public final int getSettlDay() {
        return settlDay;
    }

    @NonNull
    @Override
    public final String getRef() {
        return ref;
    }

    @Override
    public final State getState() {
        return state;
    }

    @Override
    public final Action getAction() {
        return action;
    }

    @Override
    public final long getTicks() {
        return ticks;
    }

    @Override
    public final long getLots() {
        return lots;
    }

    @Override
    public final long getResd() {
        return resd;
    }

    @Override
    public final long getExec() {
        return exec;
    }

    @Override
    public final long getCost() {
        return cost;
    }

    @Override
    public final double getAvgTicks() {
        return exec != 0 ? (double) cost / exec : 0;
    }

    @Override
    public final long getLastTicks() {
        return lastTicks;
    }

    @Override
    public final long getLastLots() {
        return lastLots;
    }

    @Override
    public final long getMinLots() {
        return minLots;
    }

    @Override
    public final boolean isDone() {
        return resd == 0;
    }

    public final long getCreated() {
        return created;
    }

    public final long getModified() {
        return modified;
    }
}

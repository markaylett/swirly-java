/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.IdUtil.newId;
import static com.swirlycloud.twirly.util.JsonUtil.getIdOrMnem;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.collection.BasicRbSlNode;
import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.util.Identifiable;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public final class Exec extends BasicRbSlNode implements Identifiable, Jsonifiable, Instruct {

    private final transient long key;
    private final long id;
    private final long orderId;
    /**
     * The executing trader.
     */
    private Identifiable trader;
    private Identifiable contr;
    private final int settlDay;
    /**
     * Ref is optional.
     */
    private final String ref;
    private State state;
    private final Action action;
    private final long ticks;
    /**
     * Must be greater than zero.
     */
    private long lots;
    /**
     * Must be greater than zero.
     */
    private long resd;
    /**
     * Must not be greater that lots.
     */
    private long exec;
    private long lastTicks;
    private long lastLots;
    /**
     * Minimum to be filled by this order.
     */
    private final long minLots;
    private long matchId;
    private Role role;
    private Identifiable cpty;
    private final long created;

    public Exec(long id, long orderId, Identifiable trader, Identifiable contr, int settlDay,
            String ref, State state, Action action, long ticks, long lots, long resd, long exec,
            long lastTicks, long lastLots, long minLots, long matchId, Role role,
            Identifiable cpty, long created) {
        if (id >= (1L << 32)) {
            throw new IllegalArgumentException("exec-id exceeds max-value");
        }
        this.key = composeKey(contr.getId(), settlDay, id);
        this.id = id;
        this.orderId = orderId;
        this.trader = trader;
        this.contr = contr;
        this.settlDay = settlDay;
        this.ref = ref != null ? ref : "";
        this.state = state;
        this.action = action;
        this.ticks = ticks;
        this.lots = lots;
        this.resd = resd;
        this.exec = exec;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.minLots = minLots;
        this.matchId = matchId;
        this.role = role;
        this.cpty = cpty;
        this.created = created;
    }

    public Exec(long id, Instruct instruct, long created) {
        if (id >= (1L << 32)) {
            throw new IllegalArgumentException("exec-id exceeds max-value");
        }
        this.key = composeKey(instruct.getContrId(), instruct.getSettlDay(), id);
        this.id = id;
        this.orderId = instruct.getOrderId();
        this.trader = instruct.getTrader();
        this.contr = instruct.getContr();
        this.settlDay = instruct.getSettlDay();
        this.ref = instruct.getRef();
        this.state = instruct.getState();
        this.action = instruct.getAction();
        this.ticks = instruct.getTicks();
        this.lots = instruct.getLots();
        this.resd = instruct.getResd();
        this.exec = instruct.getExec();
        this.lastTicks = instruct.getLastTicks();
        this.lastLots = instruct.getLastLots();
        this.minLots = instruct.getMinLots();
        this.created = created;
    }

    public static Exec parse(JsonParser p) throws IOException {
        long id = 0;
        long orderId = 0;
        Identifiable trader = null;
        Identifiable contr = null;
        int settlDay = 0;
        String ref = null;
        State state = null;
        Action action = null;
        long ticks = 0;
        long lots = 0;
        long resd = 0;
        long exec = 0;
        long lastTicks = 0;
        long lastLots = 0;
        long minLots = 0;
        long matchId = 0;
        Role role = null;
        Identifiable cpty = null;
        long created = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Exec(id, orderId, trader, contr, settlDay, ref, state, action, ticks,
                        lots, resd, exec, lastTicks, lastLots, minLots, matchId, role, cpty,
                        created);
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
                } else if ("matchId".equals(name)) {
                    matchId = 0;
                } else if ("role".equals(name)) {
                    role = null;
                } else if ("cpty".equals(name)) {
                    cpty = null;
                } else {
                    throw new IOException(String.format("unexpected null field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else if ("orderId".equals(name)) {
                    orderId = p.getLong();
                } else if ("trader".equals(name)) {
                    trader = newId(p.getLong());
                } else if ("contr".equals(name)) {
                    contr = newId(p.getLong());
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
                } else if ("lastTicks".equals(name)) {
                    lastTicks = p.getLong();
                } else if ("lastLots".equals(name)) {
                    lastLots = p.getLong();
                } else if ("minLots".equals(name)) {
                    minLots = p.getLong();
                } else if ("matchId".equals(name)) {
                    matchId = p.getLong();
                } else if ("cpty".equals(name)) {
                    cpty = newId(p.getLong());
                } else if ("created".equals(name)) {
                    created = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("ref".equals(name)) {
                    ref = p.getString();
                } else if ("state".equals(name)) {
                    state = State.valueOf(p.getString());
                } else if ("action".equals(name)) {
                    action = Action.valueOf(p.getString());
                } else if ("role".equals(name)) {
                    role = Role.valueOf(p.getString());
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
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out) throws IOException {
        out.append("{\"id\":").append(String.valueOf(id));
        out.append(",\"orderId\":").append(String.valueOf(orderId));
        out.append(",\"trader\":").append(getIdOrMnem(trader, params));
        out.append(",\"contr\":").append(getIdOrMnem(contr, params));
        out.append(",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
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
        if (lastLots != 0) {
            out.append(",\"lastTicks\":").append(String.valueOf(lastTicks));
            out.append(",\"lastLots\":").append(String.valueOf(lastLots));
        } else {
            out.append(",\"lastTicks\":null,\"lastLots\":null");
        }
        out.append(",\"minLots\":").append(String.valueOf(minLots));
        if (state == State.TRADE) {
            out.append(",\"matchId\":").append(String.valueOf(matchId));
            out.append(",\"role\":\"").append(role.name());
            out.append("\",\"cpty\":").append(getIdOrMnem(cpty, params));
        } else {
            out.append(",\"matchId\":null,\"role\":null,\"cpty\":null");
        }
        out.append(",\"created\":").append(String.valueOf(created));
        out.append("}");
    }

    public final void enrich(Trader trader, Contr contr, Trader cpty) {
        assert this.trader.getId() == trader.getId();
        assert this.contr.getId() == contr.getId();
        this.trader = trader;
        this.contr = contr;
        if (state == State.TRADE) {
            assert this.cpty.getId() == cpty.getId();
            this.cpty = cpty;
        }
    }

    /**
     * Synthetic exec key.
     */

    public static long composeKey(long contrId, int settlDay, long execId) {
        // 16 bit contr-id.
        final long CONTR_MASK = (1L << 16) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final long TJD_MASK = (1L << 16) - 1;
        // 32 bit exec-id.
        final long EXEC_MASK = (1L << 32) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = JulianDay.jdToTjd(settlDay);
        return ((contrId & CONTR_MASK) << 48) | ((tjd & TJD_MASK) << 32) | (execId & EXEC_MASK);
    }

    public final void revise(long lots) {
        state = State.REVISE;
        final long delta = this.lots - lots;
        assert delta >= 0;
        this.lots = lots;
        resd -= delta;
    }

    public final void cancel() {
        state = State.CANCEL;
        resd = 0;
    }

    public final void trade(long lots, long lastTicks, long lastLots, long matchId, Role role,
            Trader cpty) {
        state = State.TRADE;
        resd -= lots;
        exec += lots;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.matchId = matchId;
        this.role = role;
        this.cpty = cpty;
    }

    public final void trade(long lastTicks, long lastLots, long matchId, Role role, Trader cpty) {
        trade(lastLots, lastTicks, lastLots, matchId, role, cpty);
    }

    @Override
    public final long getKey() {
        return key;
    }

    @Override
    public final long getId() {
        return id;
    }

    @Override
    public final long getOrderId() {
        return orderId;
    }

    @Override
    public final long getTraderId() {
        return trader.getId();
    }

    @Override
    public final Trader getTrader() {
        return (Trader) trader;
    }

    @Override
    public final long getContrId() {
        return contr.getId();
    }

    @Override
    public final Contr getContr() {
        return (Contr) contr;
    }

    @Override
    public final int getSettlDay() {
        return settlDay;
    }

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

    public final long getMatchId() {
        return matchId;
    }

    public final Role getRole() {
        return role;
    }

    public final long getCptyId() {
        return cpty != null ? cpty.getId() : 0;
    }

    public final Trader getCpty() {
        return (Trader) cpty;
    }

    public final long getCreated() {
        return created;
    }
}

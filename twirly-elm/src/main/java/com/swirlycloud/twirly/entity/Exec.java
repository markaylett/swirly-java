/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.node.JslNode;
import com.swirlycloud.twirly.util.Params;

/**
 * A transaction that occurs as an {@link Order} transitions through a workflow.
 * 
 * Trade executions represent the exchange of goods or services between counter-parties.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class Exec extends AbstractRequest implements JslNode, Instruct {

    private static final long serialVersionUID = 1L;

    private transient @Nullable JslNode jslNext;

    private final long orderId;
    private final long quoteId;
    private State state;
    private final long ticks;
    /**
     * Must be greater than zero.
     */
    private long resd;
    /**
     * Must not be greater that lots.
     */
    private long exec;
    private long cost;
    private long lastLots;
    private long lastTicks;
    /**
     * Minimum to be filled by this order.
     */
    private final long minLots;
    private long matchId;
    private @Nullable Role role;
    private @Nullable String cpty;

    protected Exec(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, long orderId, long quoteId, State state, Side side, long lots,
            long ticks, long resd, long exec, long cost, long lastLots, long lastTicks,
            long minLots, long matchId, @Nullable Role role, @Nullable String cpty, long created) {
        super(trader, market, contr, settlDay, id, ref, side, lots, created);
        this.orderId = orderId;
        this.quoteId = quoteId;
        this.state = state;
        this.ticks = ticks;
        this.resd = resd;
        this.exec = exec;
        this.cost = cost;
        this.lastLots = lastLots;
        this.lastTicks = lastTicks;
        this.minLots = minLots;
        this.matchId = matchId;
        this.role = role;
        this.cpty = cpty;
    }

    /**
     * Special factory method for manual adjustments.
     */
    public static Exec manual(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, Side side, long lots, long ticks, @Nullable Role role,
            @Nullable String cpty, long created) {
        final long orderId = 0;
        final long quoteId = 0;
        final State state = State.TRADE;
        final long resd = 0;
        final long exec = lots;
        final long cost = ticks * lots;
        final long lastLots = lots;
        final long lastTicks = ticks;
        final long minLots = 1;
        final long matchId = 0;
        return new Exec(trader, market, contr, settlDay, id, ref, orderId, quoteId, state, side,
                lots, ticks, resd, exec, cost, lastLots, lastTicks, minLots, matchId, role, cpty,
                created);
    }

    public static Exec parse(JsonParser p) throws IOException {
        String trader = null;
        String market = null;
        String contr = null;
        int settlDay = 0;
        long id = 0;
        String ref = null;
        long orderId = 0;
        long quoteId = 0;
        State state = null;
        Side side = null;
        long lots = 0;
        long ticks = 0;
        long resd = 0;
        long exec = 0;
        long cost = 0;
        long lastLots = 0;
        long lastTicks = 0;
        long minLots = 0;
        long matchId = 0;
        Role role = null;
        String cpty = null;
        long created = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                if (trader == null) {
                    throw new IOException("trader is null");
                }
                if (market == null) {
                    throw new IOException("market is null");
                }
                if (contr == null) {
                    throw new IOException("contr is null");
                }
                if (state == null) {
                    throw new IOException("state is null");
                }
                if (side == null) {
                    throw new IOException("side is null");
                }
                return new Exec(trader, market, contr, settlDay, id, ref, orderId, quoteId, state,
                        side, lots, ticks, resd, exec, cost, lastLots, lastTicks, minLots, matchId,
                        role, cpty, created);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NULL:
                if ("settlDate".equals(name)) {
                    settlDay = 0;
                } else if ("ref".equals(name)) {
                    ref = "";
                } else if ("lastLots".equals(name)) {
                    lastLots = 0;
                } else if ("lastTicks".equals(name)) {
                    lastTicks = 0;
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
                if ("settlDate".equals(name)) {
                    settlDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("id".equals(name)) {
                    id = p.getLong();
                } else if ("orderId".equals(name)) {
                    orderId = p.getLong();
                } else if ("quoteId".equals(name)) {
                    quoteId = p.getLong();
                } else if ("lots".equals(name)) {
                    lots = p.getLong();
                } else if ("ticks".equals(name)) {
                    ticks = p.getLong();
                } else if ("resd".equals(name)) {
                    resd = p.getLong();
                } else if ("exec".equals(name)) {
                    exec = p.getLong();
                } else if ("cost".equals(name)) {
                    cost = p.getLong();
                } else if ("lastLots".equals(name)) {
                    lastLots = p.getLong();
                } else if ("lastTicks".equals(name)) {
                    lastTicks = p.getLong();
                } else if ("minLots".equals(name)) {
                    minLots = p.getLong();
                } else if ("matchId".equals(name)) {
                    matchId = p.getLong();
                } else if ("created".equals(name)) {
                    created = p.getLong();
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
                    final String s = p.getString();
                    assert s != null;
                    state = State.valueOf(s);
                } else if ("side".equals(name)) {
                    final String s = p.getString();
                    assert s != null;
                    side = Side.valueOf(s);
                } else if ("role".equals(name)) {
                    role = Role.valueOf(p.getString());
                } else if ("cpty".equals(name)) {
                    cpty = p.getString();
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

    public final Exec inverse(long id) {
        final String cpty = this.cpty;
        if (cpty == null) {
            throw new IllegalArgumentException("cpty is null");
        }
        Role role = this.role;
        if (role != null) {
            role = role.inverse();
        }
        return new Exec(cpty, market, contr, settlDay, id, ref, orderId, quoteId, state,
                side.inverse(), lots, ticks, resd, exec, cost, lastLots, lastTicks, minLots,
                matchId, role, trader, created);
    }

    @Override
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        out.append("{\"trader\":\"").append(trader);
        out.append("\",\"market\":\"").append(market);
        out.append("\",\"contr\":\"").append(contr);
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
        out.append(",\"id\":").append(String.valueOf(id));
        out.append(",\"ref\":");
        if (ref != null) {
            out.append('"').append(ref).append('"');
        } else {
            out.append("null");
        }
        out.append(",\"orderId\":").append(String.valueOf(orderId));
        out.append(",\"state\":\"").append(state.name());
        out.append("\",\"side\":\"").append(side.name());
        out.append("\",\"lots\":").append(String.valueOf(lots));
        out.append(",\"ticks\":").append(String.valueOf(ticks));
        out.append(",\"resd\":").append(String.valueOf(resd));
        out.append(",\"exec\":").append(String.valueOf(exec));
        out.append(",\"cost\":").append(String.valueOf(cost));
        if (lastLots != 0) {
            out.append(",\"lastLots\":").append(String.valueOf(lastLots));
            out.append(",\"lastTicks\":").append(String.valueOf(lastTicks));
        } else {
            out.append(",\"lastLots\":null,\"lastTicks\":null");
        }
        out.append(",\"minLots\":").append(String.valueOf(minLots));
        out.append(",\"matchId\":");
        if (matchId != 0) {
            out.append(String.valueOf(matchId));
        } else {
            out.append("null");
        }
        out.append(",\"role\":");
        final Role role = this.role;
        if (role != null) {
            out.append('"').append(role.name()).append('"');
        } else {
            out.append("null");
        }
        out.append(",\"cpty\":");
        if (cpty != null) {
            out.append('"').append(cpty).append('"');
        } else {
            out.append("null");
        }
        out.append(",\"created\":").append(String.valueOf(created));
        out.append("}");
    }

    @Override
    public final void setJslNext(@Nullable JslNode next) {
        this.jslNext = next;
    }

    @Override
    public final @Nullable JslNode jslNext() {
        return jslNext;
    }

    public final void revise(long lots) {
        state = State.REVISE;
        final long delta = this.lots - lots;
        assert delta >= 0;
        this.lots = lots;
        resd -= delta;
    }

    public final void cancel(long quotd) {
        if (quotd == 0) {
            state = State.CANCEL;
            resd = 0;
        } else {
            state = State.PECAN;
        }
    }

    public final void trade(long sumLots, long sumCost, long lastLots, long lastTicks, long matchId,
            Role role, String cpty) {
        state = State.TRADE;
        resd -= sumLots;
        exec += sumLots;
        cost += sumCost;
        this.lastLots = lastLots;
        this.lastTicks = lastTicks;
        this.matchId = matchId;
        this.role = role;
        this.cpty = cpty;
    }

    public final void trade(long lastLots, long lastTicks, long matchId, Role role, String cpty) {
        trade(lastLots, lastLots * lastTicks, lastLots, lastTicks, matchId, role, cpty);
    }

    @Override
    public final long getOrderId() {
        return orderId;
    }

    @Override
    public final long getQuoteId() {
        return quoteId;
    }

    @Override
    public final State getState() {
        return state;
    }

    @Override
    public final long getTicks() {
        return ticks;
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
    public final long getLastLots() {
        return lastLots;
    }

    @Override
    public final long getLastTicks() {
        return lastTicks;
    }

    @Override
    public final long getMinLots() {
        return minLots;
    }

    @Override
    public final boolean isDone() {
        return resd == 0;
    }

    /**
     * @return true if this execution is an automated trade.
     */
    public final boolean isAuto() {
        return matchId != 0;
    }

    public final long getMatchId() {
        return matchId;
    }

    public final @Nullable Role getRole() {
        return role;
    }

    public final @Nullable String getCpty() {
        return cpty;
    }
}

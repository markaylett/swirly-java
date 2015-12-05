/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.node.DlNode;
import com.swirlycloud.twirly.node.DlUtil;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Params;

/**
 * An instruction to buy or sell goods or services.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class Order extends AbstractRequest implements DlNode, Instruct {

    private static final long serialVersionUID = 1L;

    private transient DlNode dlPrev = DlUtil.NULL;
    private transient DlNode dlNext = DlUtil.NULL;

    // Internals.
    private transient @Nullable RbNode level;

    private final long quoteId;
    private State state;
    private final long ticks;
    /**
     * Must be greater than zero.
     */
    private long resd;
    private transient long quotd;
    /**
     * Must not be greater that lots.
     */
    private long exec;
    private long cost;
    private long lastLots;
    private long lastTicks;
    /**
     * Minimum to be filled by this
     */
    private final long minLots;
    private boolean pecan;
    private long modified;

    protected Order(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, long quoteId, State state, Side side, long lots, long ticks,
            long resd, long exec, long cost, long lastLots, long lastTicks, long minLots,
            boolean pecan, long created, long modified) {
        super(trader, market, contr, settlDay, id, ref, side, lots, created);
        assert lots > 0 && lots >= minLots;
        this.quoteId = quoteId;
        this.state = state;
        this.ticks = ticks;
        this.resd = resd;
        this.quotd = 0;
        this.exec = exec;
        this.cost = cost;
        this.lastLots = lastLots;
        this.lastTicks = lastTicks;
        this.minLots = minLots;
        this.pecan = pecan;
        this.modified = modified;
    }

    public static Order parse(JsonParser p) throws IOException {
        String trader = null;
        String market = null;
        String contr = null;
        int settlDay = 0;
        long id = 0;
        String ref = null;
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
        boolean pecan = false;
        long created = 0;
        long modified = 0;

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
                return new Order(trader, market, contr, settlDay, id, ref, quoteId, state, side,
                        lots, ticks, resd, exec, cost, lastLots, lastTicks, minLots, pecan, created,
                        modified);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_FALSE:
                if ("pecan".equals(name)) {
                    pecan = false;
                }
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
                } else {
                    throw new IOException(String.format("unexpected null field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("settlDate".equals(name)) {
                    settlDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("id".equals(name)) {
                    id = p.getLong();
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
                    final String s = p.getString();
                    assert s != null;
                    state = State.valueOf(s);
                } else if ("side".equals(name)) {
                    final String s = p.getString();
                    assert s != null;
                    side = Side.valueOf(s);
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            case VALUE_TRUE:
                if ("pecan".equals(name)) {
                    pecan = true;
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
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
        out.append(",\"quoteId\":").append(String.valueOf(quoteId));
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
        out.append(",\"pecan\":").append(String.valueOf(pecan));
        out.append(",\"created\":").append(String.valueOf(created));
        out.append(",\"modified\":").append(String.valueOf(modified));
        out.append("}");
    }

    @Override
    public final void insert(DlNode prev, DlNode next) {

        prev.setDlNext(this);
        this.setDlPrev(prev);

        next.setDlPrev(this);
        this.setDlNext(next);
    }

    @Override
    public final void insertBefore(DlNode next) {
        insert(next.dlPrev(), next);
    }

    @Override
    public final void insertAfter(DlNode prev) {
        insert(prev, prev.dlNext());
    }

    @Override
    public final void remove() {
        dlNext().setDlPrev(dlPrev);
        dlPrev().setDlNext(dlNext);
        setDlPrev(DlUtil.NULL);
        setDlNext(DlUtil.NULL);
    }

    @Override
    public void setDlPrev(@NonNull DlNode prev) {
        this.dlPrev = prev;
    }

    @Override
    public void setDlNext(@NonNull DlNode next) {
        this.dlNext = next;
    }

    public final void setLevel(@Nullable RbNode level) {
        this.level = level;
    }

    @Override
    public final DlNode dlPrev() {
        return this.dlPrev;
    }

    @Override
    public final DlNode dlNext() {
        return this.dlNext;
    }

    public final @Nullable RbNode getLevel() {
        return level;
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    /**
     * Invalidate mutable fields. Unit-testing only.
     */
    public final void invalidate() {
        state = State.NONE;
        resd = -1;
        quotd = -1;
        exec = -1;
        cost = -1;
        lastLots = -1;
        lastTicks = -1;
        modified = -1;
    }

    public final void create(long now) {
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
        if (quotd <= 0) {
            state = State.CANCEL;
            // Note that executed lots is not affected.
            resd = 0;
            pecan = false;
        } else {
            state = State.PECAN;
            pecan = true;
        }
        modified = now;
    }

    public final void trade(long takenLots, long takenCost, long lastLots, long lastTicks,
            long now) {
        state = State.TRADE;
        resd -= takenLots;
        this.exec += takenLots;
        this.cost += takenCost;
        this.lastLots = lastLots;
        this.lastTicks = lastTicks;
        modified = now;
    }

    public final void trade(long lastLots, long lastTicks, long now) {
        trade(lastLots, lastLots * lastTicks, lastLots, lastTicks, now);
    }

    public final void addQuote(long lots) {
        this.quotd += lots;
    }

    public final void subQuote(long lots) {
        this.quotd -= lots;
    }

    @Override
    public final long getOrderId() {
        return id;
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

    public final long getQuotd() {
        return quotd;
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

    public final long getAvail() {
        return resd - quotd;
    }

    @Override
    public final boolean isDone() {
        return resd == 0;
    }

    public final boolean isPecan() {
        return pecan;
    }

    /**
     * An order is working when it has residual quantity and it is not pending cancellation.
     *
     * @return true if order is working.
     */
    public final boolean isWorking() {
        return resd > 0 && !pecan;
    }

    public final long getModified() {
        return modified;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.date.JulianDay.jdToIso;

import java.io.IOException;

import com.swirlycloud.collection.BasicRbSlNode;
import com.swirlycloud.date.JulianDay;
import com.swirlycloud.function.UnaryFunction;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.Jsonifiable;
import com.swirlycloud.util.StringUtil;

public final class Exec extends BasicRbSlNode implements Identifiable, Jsonifiable, Instruct {

    private final long key;
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

    private static String getRecMnem(Identifiable iden) {
        return iden instanceof Rec ? ((Rec) iden).mnem : String.valueOf(iden.getId());
    }

    public Exec(long id, long orderId, Identifiable trader, Identifiable contr, int settlDay,
            String ref, State state, Action action, long ticks, long lots, long resd, long exec,
            long lastTicks, long lastLots, long minLots, long matchId, Role role,
            Identifiable cpty, long created) {
        if (id >= (1L << 32)) {
            throw new IllegalArgumentException("exec-id exceeds max-value");
        }
        this.key = composeId(contr.getId(), settlDay, id);
        this.id = id;
        this.orderId = orderId;
        this.trader = trader;
        this.contr = contr;
        this.settlDay = settlDay;
        this.ref = ref;
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
        this.key = composeId(instruct.getContrId(), instruct.getSettlDay(), id);
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

    @Override
    public final String toString() {
        return StringUtil.toJson(this, null);
    }

    @Override
    public final void toJson(UnaryFunction<String, String> params, Appendable out)
            throws IOException {
        out.append("{\"id\":").append(String.valueOf(id));
        out.append(",\"orderId\":").append(String.valueOf(orderId));
        out.append(",\"trader\":\"").append(getRecMnem(trader));
        out.append("\",\"contr\":\"").append(getRecMnem(contr));
        out.append("\",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
        out.append(",\"ref\":\"").append(ref);
        out.append("\",\"state\":\"").append(state.name());
        out.append("\",\"action\":\"").append(action.name());
        out.append("\",\"ticks\":").append(String.valueOf(ticks));
        out.append(",\"lots\":").append(String.valueOf(lots));
        out.append(",\"resd\":").append(String.valueOf(resd));
        out.append(",\"exec\":").append(String.valueOf(exec));
        out.append(",\"lastTicks\":").append(String.valueOf(lastTicks));
        out.append(",\"lastLots\":").append(String.valueOf(lastLots));
        out.append(",\"minLots\":").append(String.valueOf(minLots));
        if (state == State.TRADE) {
            out.append(",\"matchId\":").append(String.valueOf(matchId));
            out.append(",\"role\":\"").append(role.name());
            out.append("\",\"cpty\":\"").append(getRecMnem(cpty));
            out.append("\"");
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

    public static long composeId(long contrId, int settlDay, long execId) {
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

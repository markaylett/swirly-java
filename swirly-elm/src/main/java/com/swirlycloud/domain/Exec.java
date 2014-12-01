/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.util.Date.jdToIso;

import com.swirlycloud.util.BasicRbSlNode;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.Printable;

public final class Exec extends BasicRbSlNode implements Identifiable, Printable, Instruct {

    private final long id;
    private final long orderId;
    /**
     * The executing trader.
     */
    private Identifiable user;
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
    private long created;

    private static String getRecMnem(Identifiable iden) {
        return iden instanceof Rec ? ((Rec) iden).mnem : String.valueOf(iden.getId());
    }

    public Exec(long id, long orderId, Identifiable user, Identifiable contr, int settlDay,
            String ref, State state, Action action, long ticks, long lots, long resd, long exec,
            long lastTicks, long lastLots, long minLots, long matchId, Role role,
            Identifiable cpty, long created) {
        if (id >= (1 << 32)) {
            throw new IllegalArgumentException("exec-id exceeds max-value");
        }
        this.id = id;
        this.orderId = orderId;
        this.user = user;
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

    public Exec(long id, long orderId, Instruct instruct, long created) {
        if (id >= (1 << 32)) {
            throw new IllegalArgumentException("exec-id exceeds max-value");
        }
        this.id = id;
        this.orderId = orderId;
        this.user = instruct.getUser();
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
        final StringBuilder sb = new StringBuilder();
        print(sb, null);
        return sb.toString();
    }

    @Override
    public final void print(StringBuilder sb, Object arg) {
        sb.append("{\"id\":").append(id);
        sb.append(",\"orderId\":").append(orderId);
        sb.append(",\"user\":\"").append(getRecMnem(user));
        sb.append("\",\"contr\":\"").append(getRecMnem(contr));
        sb.append("\",\"settlDate\":").append(jdToIso(settlDay));
        sb.append(",\"ref\":\"").append(ref);
        sb.append("\",\"state\":\"").append(state);
        sb.append("\",\"action\":\"").append(action);
        sb.append("\",\"ticks\":").append(ticks);
        sb.append(",\"lots\":").append(lots);
        sb.append(",\"resd\":").append(resd);
        sb.append(",\"exec\":").append(exec);
        sb.append(",\"lastTicks\":").append(lastTicks);
        sb.append(",\"lastLots\":").append(lastLots);
        sb.append(",\"minLots\":").append(minLots);
        if (state == State.TRADE) {
            sb.append(",\"matchId\":").append(matchId);
            sb.append(",\"role\":\"").append(role);
            sb.append("\",\"cpty\":\"").append(getRecMnem(cpty));
            sb.append("\"");
        }
        sb.append(",\"created\":").append(created);
        sb.append("}");
    }

    public final void enrich(User user, Contr contr, User cpty) {
        assert this.user.getId() == user.getId();
        assert this.contr.getId() == contr.getId();
        this.user = user;
        this.contr = contr;
        if (state == State.TRADE) {
            assert this.cpty.getId() == cpty.getId();
            this.cpty = cpty;
        }
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
            User cpty) {
        state = State.TRADE;
        resd -= lots;
        exec += lots;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.matchId = matchId;
        this.role = role;
        this.cpty = cpty;
    }

    public final void trade(long lastTicks, long lastLots, long matchId, Role role, User cpty) {
        trade(lastLots, lastTicks, lastLots, matchId, role, cpty);
    }

    @Override
    public final long getKey() {
        return id;
    }

    @Override
    public final long getId() {
        return id;
    }

    public final long getOrderId() {
        return orderId;
    }

    @Override
    public final long getUserId() {
        return user.getId();
    }

    @Override
    public final User getUser() {
        return (User) user;
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

    public final User getCpty() {
        return (User) cpty;
    }

    public final long getCreated() {
        return created;
    }
}

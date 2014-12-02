/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.util.Date.jdToIso;

import java.io.IOException;

import com.swirlycloud.util.AshUtil;
import com.swirlycloud.util.BasicRbDlNode;
import com.swirlycloud.util.Date;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.Jsonifiable;
import com.swirlycloud.util.RbNode;

public final class Order extends BasicRbDlNode implements Identifiable, Jsonifiable, Instruct {

    // Internals.
    // Singly-linked buckets.
    transient Order refNext;
    transient RbNode level;

    private final long key;
    private final long id;
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
    long lastTicks;
    long lastLots;
    /**
     * Minimum to be filled by this
     */
    private final long minLots;
    long created;
    long modified;

    private static String getRecMnem(Identifiable iden) {
        return iden instanceof Rec ? ((Rec) iden).mnem : String.valueOf(iden.getId());
    }

    public Order(long id, Identifiable user, Identifiable contr, int settlDay, String ref,
            State state, Action action, long ticks, long lots, long resd, long exec,
            long lastTicks, long lastLots, long minLots, long created, long modified) {
        assert user != null;
        assert contr != null;
        assert lots > 0 && lots >= minLots;
        if (id >= (1L << 32)) {
            throw new IllegalArgumentException("order-id exceeds max-value");
        }
        this.key = composeId(contr.getId(), settlDay, id);
        this.id = id;
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
        this.created = created;
        this.modified = modified;
    }

    public Order(long id, Identifiable user, Identifiable contr, int settlDay, String ref,
            Action action, long ticks, long lots, long minLots, long created) {
        assert user != null;
        assert contr != null;
        assert lots > 0 && lots >= minLots;
        if (id >= (1L << 32)) {
            throw new IllegalArgumentException("order-id exceeds max-value");
        }
        this.key = composeId(contr.getId(), settlDay, id);
        this.id = id;
        this.user = user;
        this.contr = contr;
        this.settlDay = settlDay;
        this.ref = ref;
        this.state = State.NEW;
        this.action = action;
        this.ticks = ticks;
        this.lots = lots;
        this.resd = lots;
        this.exec = 0;
        this.lastTicks = 0;
        this.lastLots = 0;
        this.minLots = minLots;
        this.created = created;
        this.modified = created;
    }

    @Override
    public final String toString() {
        return AshUtil.toJson(this, null);
    }

    @Override
    public final void toJson(Appendable out, Object arg) throws IOException {
        out.append("{\"id\":").append(String.valueOf(id));
        out.append(",\"user\":\"").append(getRecMnem(user));
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
        out.append(",\"created\":").append(String.valueOf(created));
        out.append(",\"modified\":").append(String.valueOf(modified));
        out.append("}");
    }

    public final void enrich(User user, Contr contr) {
        assert this.user.getId() == user.getId();
        assert this.contr.getId() == contr.getId();
        this.user = user;
        this.contr = contr;
    }

    /**
     * Synthetic order key.
     */

    public static long composeId(long contrId, int settlDay, long orderId) {
        // 16 bit contr-id.
        final long CONTR_MASK = (1L << 16) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final long TJD_MASK = (1L << 16) - 1;
        // 32 bit order-id.
        final long ORDER_MASK = (1L << 32) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = Date.jdToTjd(settlDay);
        return ((contrId & CONTR_MASK) << 48) | ((tjd & TJD_MASK) << 32) | (orderId & ORDER_MASK);
    }

    public final void place(long now) {
        assert lots > 0 && lots >= minLots;
        state = State.NEW;
        resd = lots;
        exec = 0;
        modified = now;
    }

    public final void revise(long lots, long now) {
        assert lots > 0;
        assert lots >= exec && lots >= minLots && lots <= lots;
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

    public final void trade(long lots, long lastTicks, long lastLots, long now) {
        state = State.TRADE;
        resd -= lots;
        this.exec += lots;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        modified = now;
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

    public final long getCreated() {
        return created;
    }

    public final long getModified() {
        return modified;
    }
}

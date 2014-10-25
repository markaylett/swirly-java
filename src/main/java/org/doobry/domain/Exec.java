/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import org.doobry.util.BasicRbSlNode;
import org.doobry.util.Identifiable;

public final class Exec extends BasicRbSlNode implements Identifiable, Instruct {

    private final long id;
    private final long orderId;
    /**
     * The executing trader.
     */
    private Identifiable trader;
    /**
     * The give-up counter-party.
     */
    private Identifiable giveup;
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

    public Exec(long id, long order, Instruct instruct, long created) {
        this.id = id;
        this.orderId = order;
        this.trader = instruct.getTrader();
        this.giveup = instruct.getGiveup();
        this.contr = instruct.getContr();
        this.settlDay = instruct.getSettlDay();
        this.ref = instruct.getRef();
        this.state = instruct.getState();
        this.action = instruct.getAction();
        this.ticks = instruct.getTicks();
        this.lots = instruct.getLots();
        this.resd = instruct.getResd();
        this.exec = instruct.getExec();
        this.lastTicks = instruct.getTicks();
        this.lastLots = instruct.getLastLots();
        this.minLots = instruct.getMinLots();
        this.created = created;
    }

    public final void enrich(Party trader, Party giveup, Contr contr, Identifiable cpty) {
        assert this.trader.getId() == trader.getId();
        assert this.giveup.getId() == giveup.getId();
        assert this.contr.getId() == contr.getId();
        assert this.cpty.getId() == cpty.getId();
        this.trader = trader;
        this.giveup = giveup;
        this.contr = contr;
        this.cpty = cpty;
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
            Party cpty) {
        state = State.TRADE;
        resd -= lots;
        exec += lots;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.matchId = matchId;
        this.role = role;
        this.cpty = cpty;
    }

    @Override
    public final long getKey() {
        return id;
    }

    @Override
    public final long getId() {
        return id;
    }

    public final long getOrder() {
        return orderId;
    }

    @Override
    public final long getTraderId() {
        return trader.getId();
    }

    @Override
    public final Party getTrader() {
        return (Party) trader;
    }

    @Override
    public final long getGiveupId() {
        return giveup.getId();
    }

    @Override
    public final Party getGiveup() {
        return (Party) giveup;
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

    public final Party getCpty() {
        return (Party) cpty;
    }

    public final long getCreated() {
        return created;
    }
}

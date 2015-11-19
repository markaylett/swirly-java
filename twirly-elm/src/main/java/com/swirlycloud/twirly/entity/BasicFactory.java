/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.util.Memorable;

public @NonNullByDefault class BasicFactory implements Factory {

    @Override
    public Asset newAsset(String mnem, @Nullable String display, AssetType type) {
        return new Asset(mnem, display, type);
    }

    @Override
    public Contr newContr(String mnem, @Nullable String display, Memorable asset, Memorable ccy,
            int lotNumer, int lotDenom, int tickNumer, int tickDenom, int pipDp, long minLots,
            long maxLots) {
        return new Contr(mnem, display, asset, ccy, lotNumer, lotDenom, tickNumer, tickDenom, pipDp,
                minLots, maxLots);
    }

    @Override
    public Market newMarket(String mnem, @Nullable String display, Memorable contr, int settlDay,
            int expiryDay, int state, long lastLots, long lastTicks, long lastTime, long maxOrderId,
            long maxExecId, long maxQuoteId) {
        // Note that last five arguments are unused in this base implementation.
        return new Market(mnem, display, contr, settlDay, expiryDay, state);
    }

    @Override
    public final Market newMarket(String mnem, @Nullable String display, Memorable contr,
            int settlDay, int expiryDay, int state) {
        return newMarket(mnem, display, contr, settlDay, expiryDay, state, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public Trader newTrader(String mnem, @Nullable String display, String email) {
        return new Trader(mnem, display, email);
    }

    @Override
    public Order newOrder(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, State state, Side side, long lots, long ticks, long resd,
            long exec, long cost, long lastLots, long lastTicks, long minLots, boolean pecan,
            long created, long modified) {
        return new Order(trader, market, contr, settlDay, id, ref, state, side, lots, ticks, resd,
                exec, cost, lastLots, lastTicks, minLots, pecan, created, modified);
    }

    @Override
    public final Order newOrder(String trader, Financial fin, long id, @Nullable String ref,
            State state, Side side, long lots, long ticks, long resd, long exec, long cost,
            long lastLots, long lastTicks, long minLots, boolean pecan, long created,
            long modified) {
        return newOrder(trader, fin.getMarket(), fin.getContr(), fin.getSettlDay(), id, ref, state,
                side, lots, ticks, resd, exec, cost, lastLots, lastTicks, minLots, pecan, created,
                modified);
    }

    @Override
    public final Order newOrder(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, Side side, long lots, long ticks, long minLots, long created) {
        return newOrder(trader, market, contr, settlDay, id, ref, State.NEW, side, lots, ticks,
                lots, 0, 0, 0, 0, minLots, false, created, created);
    }

    @Override
    public final Order newOrder(String trader, Financial fin, long id, @Nullable String ref,
            Side side, long lots, long ticks, long minLots, long created) {
        return newOrder(trader, fin.getMarket(), fin.getContr(), fin.getSettlDay(), id, ref,
                State.NEW, side, lots, ticks, lots, 0, 0, 0, 0, minLots, false, created, created);
    }

    @Override
    public Exec newExec(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, long orderId, State state, Side side, long lots, long ticks,
            long resd, long exec, long cost, long lastLots, long lastTicks, long minLots,
            long matchId, @Nullable Role role, @Nullable String cpty, long created) {
        return new Exec(trader, market, contr, settlDay, id, ref, orderId, state, side, lots, ticks,
                resd, exec, cost, lastLots, lastTicks, minLots, matchId, role, cpty, created);
    }

    @Override
    public final Exec newExec(String trader, Financial fin, long id, @Nullable String ref,
            long orderId, State state, Side side, long lots, long ticks, long resd, long exec,
            long cost, long lastLots, long lastTicks, long minLots, long matchId,
            @Nullable Role role, @Nullable String cpty, long created) {
        return newExec(trader, fin.getMarket(), fin.getContr(), fin.getSettlDay(), id, ref, orderId,
                state, side, lots, ticks, resd, exec, cost, lastLots, lastTicks, minLots, matchId,
                role, cpty, created);
    }

    @Override
    public final Exec newExec(Instruct instruct, long id, long created) {
        return newExec(instruct.getTrader(), instruct.getMarket(), instruct.getContr(),
                instruct.getSettlDay(), id, instruct.getRef(), instruct.getOrderId(),
                instruct.getState(), instruct.getSide(), instruct.getLots(), instruct.getTicks(),
                instruct.getResd(), instruct.getExec(), instruct.getCost(), instruct.getLastLots(),
                instruct.getLastTicks(), instruct.getMinLots(), 0, null, null, created);
    }

    @Override
    public Posn newPosn(String trader, String contr, int settlDay, long buyLots, long buyCost,
            long sellLots, long sellCost) {
        return new Posn(trader, contr, settlDay, buyLots, buyCost, sellLots, sellCost);
    }

    @Override
    public final Posn newPosn(String trader, String contr, int settlDay) {
        return newPosn(trader, contr, settlDay, 0, 0, 0, 0);
    }

    @Override
    public final Quote newQuote(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, @Nullable Order order, Side side, long lots, long ticks,
            long created, long expiry) {
        return new Quote(trader, market, contr, settlDay, id, ref, order, side, lots, ticks,
                created, expiry);
    }

    @Override
    public final Quote newQuote(String trader, Financial fin, long id, @Nullable String ref,
            @Nullable Order order, Side side, long lots, long ticks, long created, long expiry) {
        return new Quote(trader, fin.getMarket(), fin.getContr(), fin.getSettlDay(), id, ref, order,
                side, lots, ticks, created, expiry);
    }
}

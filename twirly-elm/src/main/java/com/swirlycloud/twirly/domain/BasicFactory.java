/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.util.Memorable;

public @NonNullByDefault class BasicFactory implements Factory {

    @Override
    public Asset newAsset(String mnem, @Nullable String display, AssetType type) {
        return new Asset(mnem, display, type);
    }

    @Override
    public Contr newContr(String mnem, @Nullable String display, Memorable asset, Memorable ccy,
            int tickNumer, int tickDenom, int lotNumer, int lotDenom, int pipDp, long minLots,
            long maxLots) {
        return new Contr(mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer, lotDenom,
                pipDp, minLots, maxLots);
    }

    @Override
    public Market newMarket(String mnem, @Nullable String display, Memorable contr, int settlDay,
            int expiryDay, int state, long lastTicks, long lastLots, long lastTime,
            long maxOrderId, long maxExecId) {
        return new Market(mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots,
                lastTime, maxOrderId, maxExecId);
    }

    @Override
    public final Market newMarket(String mnem, @Nullable String display, Memorable contr,
            int settlDay, int expiryDay, int state) {
        return newMarket(mnem, display, contr, settlDay, expiryDay, state, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public Trader newTrader(String mnem, @Nullable String display, String email) {
        return new Trader(mnem, display, email);
    }

    @Override
    public Order newOrder(long id, String trader, String market, String contr, int settlDay,
            @Nullable String ref, State state, Action action, long ticks, long lots, long resd,
            long exec, long cost, long lastTicks, long lastLots, long minLots, long created,
            long modified) {
        return new Order(id, trader, market, contr, settlDay, ref, state, action, ticks, lots,
                resd, exec, cost, lastTicks, lastLots, minLots, created, modified);
    }

    @Override
    public final Order newOrder(long id, String trader, Financial fin, @Nullable String ref,
            State state, Action action, long ticks, long lots, long resd, long exec, long cost,
            long lastTicks, long lastLots, long minLots, long created, long modified) {
        return newOrder(id, trader, fin.getMarket(), fin.getContr(), fin.getSettlDay(), ref, state,
                action, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, created,
                modified);
    }

    @Override
    public final Order newOrder(long id, String trader, String market, String contr, int settlDay,
            @Nullable String ref, Action action, long ticks, long lots, long minLots, long created) {
        return newOrder(id, trader, market, contr, settlDay, ref, State.NEW, action, ticks, lots,
                lots, 0, 0, 0, 0, minLots, created, created);
    }

    @Override
    public final Order newOrder(long id, String trader, Financial fin, @Nullable String ref,
            Action action, long ticks, long lots, long minLots, long created) {
        return newOrder(id, trader, fin.getMarket(), fin.getContr(), fin.getSettlDay(), ref,
                State.NEW, action, ticks, lots, lots, 0, 0, 0, 0, minLots, created, created);
    }

    @Override
    public Exec newExec(long id, long orderId, String trader, String market, String contr,
            int settlDay, @Nullable String ref, State state, Action action, long ticks, long lots,
            long resd, long exec, long cost, long lastTicks, long lastLots, long minLots,
            long matchId, @Nullable Role role, @Nullable String cpty, long created) {
        return new Exec(id, orderId, trader, market, contr, settlDay, ref, state, action, ticks,
                lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, role, cpty, created);
    }

    @Override
    public final Exec newExec(long id, long orderId, String trader, Financial fin,
            @Nullable String ref, State state, Action action, long ticks, long lots, long resd,
            long exec, long cost, long lastTicks, long lastLots, long minLots, long matchId,
            @Nullable Role role, @Nullable String cpty, long created) {
        return newExec(id, orderId, trader, fin.getMarket(), fin.getContr(), fin.getSettlDay(),
                ref, state, action, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots,
                matchId, role, cpty, created);
    }

    @Override
    public final Exec newExec(long id, Instruct instruct, long created) {
        return newExec(id, instruct.getOrderId(), instruct.getTrader(), instruct.getMarket(),
                instruct.getContr(), instruct.getSettlDay(), instruct.getRef(),
                instruct.getState(), instruct.getAction(), instruct.getTicks(), instruct.getLots(),
                instruct.getResd(), instruct.getExec(), instruct.getCost(),
                instruct.getLastTicks(), instruct.getLastLots(), instruct.getMinLots(), 0, null,
                null, created);
    }

    @Override
    public Posn newPosn(String trader, String contr, int settlDay, long buyCost, long buyLots,
            long sellCost, long sellLots) {
        return new Posn(trader, contr, settlDay, buyCost, buyLots, sellCost, sellLots);
    }

    @Override
    public final Posn newPosn(String trader, String contr, int settlDay) {
        return newPosn(trader, contr, settlDay, 0, 0, 0, 0);
    }
}

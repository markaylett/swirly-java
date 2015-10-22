/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.rec.Asset;
import com.swirlycloud.twirly.rec.AssetType;
import com.swirlycloud.twirly.rec.Contr;
import com.swirlycloud.twirly.rec.Market;
import com.swirlycloud.twirly.rec.Trader;
import com.swirlycloud.twirly.util.Memorable;

public @NonNullByDefault interface Factory {

    Asset newAsset(String mnem, @Nullable String display, AssetType type);

    Contr newContr(String mnem, @Nullable String display, Memorable asset, Memorable ccy,
            int tickNumer, int tickDenom, int lotNumer, int lotDenom, int pipDp, long minLots,
            long maxLots);

    Market newMarket(String mnem, @Nullable String display, Memorable contr, int settlDay,
            int expiryDay, int state, long lastTicks, long lastLots, long lastTime, long maxOrderId,
            long maxExecId);

    Market newMarket(String mnem, @Nullable String display, Memorable contr, int settlDay,
            int expiryDay, int state);

    Trader newTrader(String mnem, @Nullable String display, String email);

    Order newOrder(long id, String trader, String market, String contr, int settlDay,
            @Nullable String ref, State state, Side side, long ticks, long lots, long resd,
            long exec, long cost, long lastTicks, long lastLots, long minLots, boolean pecan,
            long created, long modified);

    Order newOrder(long id, String trader, Financial fin, @Nullable String ref, State state,
            Side side, long ticks, long lots, long resd, long exec, long cost, long lastTicks,
            long lastLots, long minLots, boolean pecan, long created, long modified);

    Order newOrder(long id, String trader, String market, String contr, int settlDay,
            @Nullable String ref, Side side, long ticks, long lots, long minLots, long created);

    Order newOrder(long id, String trader, Financial fin, @Nullable String ref, Side side,
            long ticks, long lots, long minLots, long created);

    Exec newExec(long id, long orderId, String trader, String market, String contr, int settlDay,
            @Nullable String ref, State state, Side side, long ticks, long lots, long resd,
            long exec, long cost, long lastTicks, long lastLots, long minLots, long matchId,
            @Nullable Role role, @Nullable String cpty, long created);

    Exec newExec(long id, long orderId, String trader, Financial fin, @Nullable String ref,
            State state, Side side, long ticks, long lots, long resd, long exec, long cost,
            long lastTicks, long lastLots, long minLots, long matchId, @Nullable Role role,
            @Nullable String cpty, long created);

    Exec newExec(long id, Instruct instruct, long created);

    Posn newPosn(String trader, String contr, int settlDay, long buyCost, long buyLots,
            long sellCost, long sellLots);

    Posn newPosn(String trader, String contr, int settlDay);
}

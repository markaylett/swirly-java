/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.intrusive.RefHashTable;
import com.swirlycloud.twirly.util.Memorable;

public final @NonNullByDefault class ServFactory extends BasicFactory {

    private static final int CAPACITY = 1 << 5; // 64

    private final RefHashTable refIdx = new RefHashTable(CAPACITY);

    @Override
    public final MarketBook newMarket(String mnem, @Nullable String display, Memorable contr,
            int settlDay, int expiryDay, int state, long lastTicks, long lastLots, long lastTime,
            long maxOrderId, long maxExecId) {
        return new MarketTag(mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots,
                lastTime, maxOrderId, maxExecId);
    }

    @Override
    public final TraderSess newTrader(String mnem, @Nullable String display, String email) {
        return new TraderTag(mnem, display, email, refIdx, this);
    }
}
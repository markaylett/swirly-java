/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.util.Memorable;

/**
 * This class is required to deserialise of {@code Market} messages.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class MarketBook extends Market {

    private static final long serialVersionUID = 1L;

    MarketBook(String mnem, @Nullable String display, Memorable contr, int settlDay, int expiryDay,
            int state, long lastTicks, long lastLots, long lastTime) {
        super(mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots, lastTime);
    }
}

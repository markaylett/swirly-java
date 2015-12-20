/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.tag;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.book.MarketBook;

/**
 * A marker interface use for {@code Market} serialisation.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class MarketTag extends MarketBook {

    private static final long serialVersionUID = 1L;

    public MarketTag(String mnem, @Nullable String display, String contr, int settlDay,
            int expiryDay, int state, long lastLots, long lastTicks, long lastTime, long maxOrderId,
            long maxExecId, long maxQuoteId) {
        super(mnem, display, contr, settlDay, expiryDay, state, lastLots, lastTicks, lastTime,
                maxOrderId, maxExecId, maxQuoteId);
    }
}

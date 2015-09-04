/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.rec.Market;
import com.swirlycloud.twirly.util.Memorable;

/**
 * A marker interface use for {@code Market} serialisation.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class MarketTag extends Market {

    private static final long serialVersionUID = 1L;

    MarketTag(String mnem, @Nullable String display, Memorable contr, int settlDay, int expiryDay,
            int state) {
        super(mnem, display, contr, settlDay, expiryDay, state);
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.tag;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.entity.Market;

/**
 * A marker interface use for {@code Market} serialisation.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class MarketTag extends Market {

    private static final long serialVersionUID = 1L;

    MarketTag(String mnem, @Nullable String display, String contr, int settlDay, int expiryDay,
            int state) {
        super(mnem, display, contr, settlDay, expiryDay, state);
    }
}

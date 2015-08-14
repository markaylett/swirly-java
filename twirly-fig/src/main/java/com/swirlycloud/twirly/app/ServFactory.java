/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.BasicFactory;
import com.swirlycloud.twirly.intrusive.RefHashTable;

public final @NonNullByDefault class ServFactory extends BasicFactory {

    private static final int CAPACITY = 1 << 5; // 64

    private final RefHashTable refIdx = new RefHashTable(CAPACITY);

    /*
     * (non-Javadoc)
     * 
     * @see com.swirlycloud.twirly.domain.BasicFactory#newTrader(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public final TraderSess newTrader(String mnem, @Nullable String display, String email) {
        return new TraderSess(mnem, display, email, refIdx, this);
    }
}

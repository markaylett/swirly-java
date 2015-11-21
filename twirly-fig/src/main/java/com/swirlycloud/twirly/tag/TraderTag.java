/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.tag;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.entity.Factory;
import com.swirlycloud.twirly.entity.RequestRefMap;
import com.swirlycloud.twirly.entity.TraderSess;

/**
 * A marker interface use for {@code Trader} serialisation.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class TraderTag extends TraderSess {

    private static final long serialVersionUID = 1L;

    public TraderTag(String mnem, @Nullable String display, String email, RequestRefMap refIdx,
            Factory factory) {
        super(mnem, display, email, refIdx, factory);
    }
}

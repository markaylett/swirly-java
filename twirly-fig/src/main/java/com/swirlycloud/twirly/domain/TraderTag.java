/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.intrusive.RequestRefMap;

/**
 * A marker interface use for {@code Trader} serialisation.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class TraderTag extends TraderSess {

    private static final long serialVersionUID = 1L;

    TraderTag(String mnem, @Nullable String display, String email, RequestRefMap refIdx,
            Factory factory) {
        super(mnem, display, email, refIdx, factory);
    }
}

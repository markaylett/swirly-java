/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A marker interface use for {@code Trader} serialisation.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class TraderTag extends Trader {

    private static final long serialVersionUID = 1L;

    TraderTag(String mnem, @Nullable String display, String email) {
        super(mnem, display, email);
    }
}

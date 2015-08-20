/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is required to deserialise of {@code Trader} messages.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class TraderSess extends Trader {

    private static final long serialVersionUID = 1L;

    TraderSess(String mnem, @Nullable String display, String email) {
        super(mnem, display, email);
    }
}

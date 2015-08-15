/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

public final @NonNullByDefault class MnemUtil {
    private MnemUtil() {
    }

    public static Memorable newMnem(final String mnem) {
        return new Memorable() {

            private static final long serialVersionUID = 1L;

            @Override
            public final String getMnem() {
                return mnem;
            }
        };
    }
}

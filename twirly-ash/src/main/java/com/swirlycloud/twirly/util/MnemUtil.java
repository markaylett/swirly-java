/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

public final class MnemUtil {
    private MnemUtil() {
    }

    public static Memorable newMnem(final String mnem) {
        return new Memorable() {
            @Override
            public final String getMnem() {
                return mnem;
            }
        };
    }
}

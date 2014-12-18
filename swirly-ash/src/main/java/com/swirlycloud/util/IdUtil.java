/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

public final class IdUtil {
    private IdUtil() {
    }

    public static Identifiable newId(final long id) {
        return new Identifiable() {
            @Override
            public final long getId() {
                return id;
            }
        };
    }

    public static final Identifiable ZERO_ID = newId(0);
}

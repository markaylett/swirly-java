/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public final @NonNullByDefault class CacheUtil {

    private CacheUtil() {
    }

    public static final Cache NO_CACHE = new Cache() {
        @Override
        public final void close() throws Exception {
        }

        @Override
        public final @Nullable Object get(String key) {
            return null;
        }

        @Override
        public final void put(String key, Object val) {
        }
    };
}

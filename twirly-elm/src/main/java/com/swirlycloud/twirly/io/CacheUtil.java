/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.concurrent.FutureAdapter;

public final @NonNullByDefault class CacheUtil {

    private CacheUtil() {
    }

    public static final Cache NO_CACHE = new Cache() {
        @Override
        public final void close() throws Exception {
        }

        @Override
        public final Future<?> read(String key) {
            return new FutureAdapter<>(null);
        }

        @Override
        public final Future<Map<String, Object>> read(Collection<String> keys) {
            final Map<String, Object> m = new HashMap<>();
            for (final String key : keys) {
                m.put(key, null);
            }
            return new FutureAdapter<>(m);
        }

        @Override
        public final void create(String key, Object val) {
        }

        @Override
        public final void update(String key, Object val) {
        }

        @Override
        public final void delete(String key) {
        }
    };
}

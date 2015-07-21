/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public final @NonNullByDefault class SpyCache implements Cache {

    // FIXME: expiry handling.
    private static final int EXPIRY = 3600;

    private final MemcachedClient mc;

    public SpyCache(InetSocketAddress... ia) throws IOException {
        mc = new MemcachedClient(ia);
    }

    @Override
    public final void close() throws Exception {
        mc.shutdown();
    }

    @Override
    public final @Nullable Object get(String key) {
        return mc.get(key);
    }

    @Override
    public final void put(String key, Object val) {
        mc.set(key, EXPIRY, val);
    }
}
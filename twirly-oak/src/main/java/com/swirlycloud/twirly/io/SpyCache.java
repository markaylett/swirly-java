/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public final @NonNullByDefault class SpyCache implements Cache {

    // Expire after 24 hours.
    private static final int EXPIRY = 24 * 60 * 60;

    private final MemcachedClient mc;

    public SpyCache(InetSocketAddress... ia) throws IOException {
        mc = new MemcachedClient(ia);
    }

    @Override
    public final void close() throws Exception {
        mc.shutdown(5, TimeUnit.SECONDS);
    }

    @Override
    public final @Nullable Object select(String key) {
        return mc.get(key);
    }

    @Override
    public final void insert(String key, Object val) {
        mc.add(key, EXPIRY, val);
    }

    @Override
    public final void update(String key, Object val) {
        mc.set(key, EXPIRY, val);
    }

    @Override
    public final void delete(String key) {
        mc.delete(key);
    }
}
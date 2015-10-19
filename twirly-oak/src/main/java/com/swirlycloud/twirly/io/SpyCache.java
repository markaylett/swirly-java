/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.BulkFuture;

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
    public final Future<?> select(String key) {
        final Future<?> fut = mc.asyncGet(key);
        assert fut != null;
        return fut;
    }

    @Override
    public final Future<Map<String, Object>> select(Collection<String> keys) {
        final BulkFuture<Map<String, Object>> fut = mc.asyncGetBulk(keys);
        assert fut != null;
        return fut;        
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.logging.Level;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public final @NonNullByDefault class AppEngineCache implements Cache {

    // Expire after 24 hours.
    @SuppressWarnings("null")
    private static final Expiration EXPIRY = Expiration.byDeltaSeconds(24 * 60 * 60);

    private final MemcacheService ms;
    private final AsyncMemcacheService ams;

    public AppEngineCache() {
        final MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
        assert ms != null;
        ms.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        final AsyncMemcacheService ams = MemcacheServiceFactory.getAsyncMemcacheService();
        assert ams != null;
        ams.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        this.ms = ms;
        this.ams = ams;
    }

    @Override
    public final void close() throws Exception {
    }

    @Override
    public final @Nullable Object select(String key) {
        return ms.get(key);
    }

    @Override
    public final void insert(String key, Object val) {
        ams.put(key, val, EXPIRY, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
    }

    @Override
    public final void update(String key, Object val) {
        ams.put(key, val, EXPIRY, SetPolicy.SET_ALWAYS);
    }

    @Override
    public final void delete(String key) {
        ams.delete(key);
    }
}

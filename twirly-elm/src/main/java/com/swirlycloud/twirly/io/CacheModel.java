/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.node.SlNode;

public final class CacheModel implements Model {

    private final Model model;
    private final Cache cache;

    /**
     * Ownership and responsibility for closing the cache and model will transferred to the new
     * instance.
     * 
     * @param model
     *            The underlying model.
     * @param cache
     *            The cache.
     */
    public CacheModel(Model model, Cache cache) {
        this.model = model;
        this.cache = cache;
    }

    @Override
    public final void close() throws Exception {
        cache.close();
        model.close();
    }

    @Override
    public final @Nullable MnemRbTree selectAsset() throws InterruptedException {
        MnemRbTree t = (MnemRbTree) cache.select("asset");
        if (t == null) {
            t = model.selectAsset();
            assert t != null;
            cache.insert("asset", t);
        }
        return t;
    }

    @Override
    public final @Nullable MnemRbTree selectContr() throws InterruptedException {
        MnemRbTree t = (MnemRbTree) cache.select("contr");
        if (t == null) {
            t = model.selectContr();
            assert t != null;
            cache.insert("contr", t);
        }
        return t;
    }

    @Override
    public final @Nullable MnemRbTree selectMarket() throws InterruptedException {
        MnemRbTree t = (MnemRbTree) cache.select("market");
        if (t == null) {
            t = model.selectMarket();
            assert t != null;
            cache.insert("market", t);
        }
        return t;
    }

    @Override
    public final @Nullable MnemRbTree selectTrader() throws InterruptedException {
        MnemRbTree t = (MnemRbTree) cache.select("trader");
        if (t == null) {
            t = model.selectTrader();
            assert t != null;
            cache.insert("trader", t);
        }
        return t;
    }

    @Override
    public final @Nullable String selectTraderByEmail(@NonNull String email)
            throws InterruptedException {
        return model.selectTraderByEmail(email);
    }

    @Override
    public final @Nullable SlNode selectOrder() throws InterruptedException {
        return model.selectOrder();
    }

    @Override
    public final @Nullable SlNode selectTrade() throws InterruptedException {
        return model.selectTrade();
    }

    @Override
    public final @Nullable SlNode selectPosn(int busDay) throws InterruptedException {
        return model.selectPosn(busDay);
    }

    public final Cache getCache() {
        return cache;
    }
}

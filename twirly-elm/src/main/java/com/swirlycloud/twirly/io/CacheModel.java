/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
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
    public final @Nullable MnemRbTree selectAsset(@NonNull Factory factory)
            throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("asset");
        if (tree == null) {
            tree = model.selectAsset(factory);
            assert tree != null;
            cache.insert("asset", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable MnemRbTree selectContr(@NonNull Factory factory)
            throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("contr");
        if (tree == null) {
            tree = model.selectContr(factory);
            assert tree != null;
            cache.insert("contr", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable MnemRbTree selectMarket(@NonNull Factory factory)
            throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("market");
        if (tree == null) {
            tree = model.selectMarket(factory);
            assert tree != null;
            cache.insert("market", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable MnemRbTree selectTrader(@NonNull Factory factory)
            throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("trader");
        if (tree == null) {
            tree = model.selectTrader(factory);
            assert tree != null;
            cache.insert("trader", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable String selectTraderByEmail(@NonNull String email,
            @NonNull Factory factory) throws InterruptedException {
        final String key = "trader:" + email;
        String trader = (String) cache.select(key);
        if (trader == null) {
            trader = model.selectTraderByEmail(email, factory);
            // An empty value indicates that there is no trader with this email, as opposed to a
            // null value, which indicates that the cache is empty.
            cache.insert(key, trader != null ? trader : "");
        }
        return trader != null && !trader.isEmpty() ? trader : null;
    }

    @Override
    public final @Nullable SlNode selectOrder(@NonNull Factory factory) throws InterruptedException {
        return model.selectOrder(factory);
    }

    @Override
    public final @Nullable InstructTree selectOrder(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException {
        final String key = "order:" + trader;
        InstructTree tree = (InstructTree) cache.select(key);
        if (tree == null) {
            tree = model.selectOrder(trader, factory);
            assert tree != null;
            cache.insert(key, tree);
        }
        return tree;
    }

    @Override
    public final @Nullable SlNode selectTrade(@NonNull Factory factory) throws InterruptedException {
        return model.selectTrade(factory);
    }

    @Override
    public final @Nullable InstructTree selectTrade(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException {
        final String key = "trade:" + trader;
        InstructTree tree = (InstructTree) cache.select(key);
        if (tree == null) {
            tree = model.selectTrade(trader, factory);
            assert tree != null;
            cache.insert(key, tree);
        }
        return tree;
    }

    @Override
    public final @Nullable SlNode selectPosn(int busDay, @NonNull Factory factory)
            throws InterruptedException {
        return model.selectPosn(busDay, factory);
    }

    @Override
    public final @Nullable TraderPosnTree selectPosn(@NonNull String trader, int busDay,
            @NonNull Factory factory) throws InterruptedException {
        final String key = "posn:" + trader;
        TraderPosnTree tree = (TraderPosnTree) cache.select(key);
        if (tree == null) {
            tree = model.selectPosn(trader, busDay, factory);
            assert tree != null;
            cache.insert(key, tree);
        }
        return tree;
    }

    public final Cache getCache() {
        return cache;
    }
}

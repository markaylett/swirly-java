/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
    public final @Nullable MnemRbTree selectAsset() throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("asset");
        if (tree == null) {
            tree = model.selectAsset();
            assert tree != null;
            cache.insert("asset", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable MnemRbTree selectContr() throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("contr");
        if (tree == null) {
            tree = model.selectContr();
            assert tree != null;
            cache.insert("contr", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable MnemRbTree selectMarket() throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("market");
        if (tree == null) {
            tree = model.selectMarket();
            assert tree != null;
            cache.insert("market", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable MnemRbTree selectTrader() throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("trader");
        if (tree == null) {
            tree = model.selectTrader();
            assert tree != null;
            cache.insert("trader", tree);
        }
        return tree;
    }

    @Override
    public final @Nullable String selectTraderByEmail(@NonNull String email)
            throws InterruptedException {
        final String key = "trader:" + email;
        String trader = (String) cache.select(key);
        if (trader == null) {
            trader = model.selectTraderByEmail(email);
            // An empty value indicates that there is no trader with this email, as opposed to a
            // null value, which indicates that the cache is empty.
            cache.insert(key, trader != null ? trader : "");
        }
        return trader != null && !trader.isEmpty() ? trader : null;
    }

    @Override
    public final @Nullable SlNode selectOrder() throws InterruptedException {
        return model.selectOrder();
    }

    @Override
    public final @Nullable InstructTree selectOrder(@NonNull String trader) throws InterruptedException {
        final String key = "order:" + trader;
        InstructTree tree = (InstructTree) cache.select(key);
        if (tree == null) {
            tree = model.selectOrder(trader);
            assert tree != null;
            cache.insert(key, tree);
        }
        return tree;
    }

    @Override
    public final @Nullable SlNode selectTrade() throws InterruptedException {
        return model.selectTrade();
    }

    @Override
    public final @Nullable InstructTree selectTrade(@NonNull String trader) throws InterruptedException {
        final String key = "trade:" + trader;
        InstructTree tree = (InstructTree) cache.select(key);
        if (tree == null) {
            tree = model.selectTrade(trader);
            assert tree != null;
            cache.insert(key, tree);
        }
        return tree;
    }

    @Override
    public final @Nullable SlNode selectPosn(int busDay) throws InterruptedException {
        return model.selectPosn(busDay);
    }

    @Override
    public final @Nullable TraderPosnTree selectPosn(@NonNull String trader, int busDay)
            throws InterruptedException {
        final String key = "posn:" + trader;
        TraderPosnTree tree = (TraderPosnTree) cache.select(key);
        if (tree == null) {
            tree = model.selectPosn(trader, busDay);
            assert tree != null;
            cache.insert(key, tree);
        }
        return tree;
    }

    public final Cache getCache() {
        return cache;
    }
}

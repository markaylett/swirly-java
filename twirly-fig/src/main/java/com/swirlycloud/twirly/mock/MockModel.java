/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.RequestTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.SlNode;

public class MockModel implements Model {

    @Override
    public void close() {
    }

    @Override
    public @NonNull MnemRbTree readAsset(@NonNull final Factory factory) {
        return MockAsset.readAsset(factory);
    }

    @Override
    public @NonNull MnemRbTree readContr(@NonNull final Factory factory) {
        return MockContr.readContr(factory);
    }

    @Override
    public @NonNull MnemRbTree readMarket(@NonNull final Factory factory) {
        return new MnemRbTree();
    }

    @Override
    public @NonNull MnemRbTree readTrader(@NonNull final Factory factory) {
        return MockTrader.readTrader(factory);
    }

    @Override
    public @Nullable String readTraderByEmail(@NonNull String email,
            @NonNull final Factory factory) {
        return MockTrader.readTraderByEmail(email, factory);
    }

    @Override
    public final @NonNull MnemRbTree readView(@NonNull Factory factory)
            throws InterruptedException {
        return new MnemRbTree();
    }

    @Override
    public SlNode readOrder(@NonNull final Factory factory) {
        return null;
    }

    @Override
    public @NonNull RequestTree readOrder(@NonNull String trader,
            @NonNull final Factory factory) {
        return new RequestTree();
    }

    @Override
    public SlNode readTrade(@NonNull final Factory factory) {
        return null;
    }

    @Override
    public @NonNull RequestTree readTrade(@NonNull String trader,
            @NonNull final Factory factory) {
        return new RequestTree();
    }

    @Override
    public SlNode readPosn(int busDay, @NonNull final Factory factory) {
        return null;
    }

    @Override
    public @NonNull TraderPosnTree readPosn(@NonNull String trader, int busDay,
            @NonNull final Factory factory) {
        return new TraderPosnTree();
    }
}

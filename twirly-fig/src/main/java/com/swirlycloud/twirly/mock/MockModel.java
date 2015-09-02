/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.SlNode;

public class MockModel implements Model {

    @Override
    public void close() {
    }

    @Override
    public @NonNull MnemRbTree selectAsset(@NonNull final Factory factory) {
        return MockAsset.selectAsset(factory);
    }

    @Override
    public MnemRbTree selectContr(@NonNull final Factory factory) {
        return MockContr.selectContr(factory);
    }

    @Override
    public MnemRbTree selectMarket(@NonNull final Factory factory) {
        return new MnemRbTree();
    }

    @Override
    public MnemRbTree selectTrader(@NonNull final Factory factory) {
        return MockTrader.selectTrader(factory);
    }

    @Override
    public @Nullable String selectTraderByEmail(@NonNull String email,
            @NonNull final Factory factory) {
        return MockTrader.selectTraderByEmail(email, factory);
    }

    @Override
    public SlNode selectOrder(@NonNull final Factory factory) {
        return null;
    }

    @Override
    public InstructTree selectOrder(@NonNull String trader, @NonNull final Factory factory) {
        return null;
    }

    @Override
    public SlNode selectTrade(@NonNull final Factory factory) {
        return null;
    }

    @Override
    public InstructTree selectTrade(@NonNull String trader, @NonNull final Factory factory) {
        return null;
    }

    @Override
    public SlNode selectPosn(int busDay, @NonNull final Factory factory) {
        return null;
    }

    @Override
    public TraderPosnTree selectPosn(@NonNull String trader, int busDay,
            @NonNull final Factory factory) {
        return null;
    }
}

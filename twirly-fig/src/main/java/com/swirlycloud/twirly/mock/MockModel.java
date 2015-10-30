/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.intrusive.RecTree;
import com.swirlycloud.twirly.intrusive.RequestIdTree;
import com.swirlycloud.twirly.intrusive.MarketViewTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.SlNode;

public class MockModel implements Model {

    @Override
    public void close() {
    }

    @Override
    public @NonNull RecTree readAsset(@NonNull final Factory factory) {
        return MockAsset.readAsset(factory);
    }

    @Override
    public @NonNull RecTree readContr(@NonNull final Factory factory) {
        return MockContr.readContr(factory);
    }

    @Override
    public @NonNull RecTree readMarket(@NonNull final Factory factory) {
        return new RecTree();
    }

    @Override
    public @NonNull RecTree readTrader(@NonNull final Factory factory) {
        return MockTrader.readTrader(factory);
    }

    @Override
    public @Nullable String readTraderByEmail(@NonNull String email,
            @NonNull final Factory factory) {
        return MockTrader.readTraderByEmail(email, factory);
    }

    @Override
    public final @NonNull MarketViewTree readView(@NonNull Factory factory)
            throws InterruptedException {
        return new MarketViewTree();
    }

    @Override
    public SlNode readOrder(@NonNull final Factory factory) {
        return null;
    }

    @Override
    public @NonNull RequestIdTree readOrder(@NonNull String trader, @NonNull final Factory factory) {
        return new RequestIdTree();
    }

    @Override
    public SlNode readTrade(@NonNull final Factory factory) {
        return null;
    }

    @Override
    public @NonNull RequestIdTree readTrade(@NonNull String trader, @NonNull final Factory factory) {
        return new RequestIdTree();
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

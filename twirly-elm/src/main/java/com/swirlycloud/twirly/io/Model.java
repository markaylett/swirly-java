/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.node.SlNode;

public interface Model extends AutoCloseable {

    @Nullable
    MnemRbTree selectAsset() throws InterruptedException;

    @Nullable
    MnemRbTree selectContr() throws InterruptedException;

    @Nullable
    MnemRbTree selectMarket() throws InterruptedException;

    @Nullable
    MnemRbTree selectTrader() throws InterruptedException;

    @Nullable
    String selectTraderByEmail(@NonNull String email) throws InterruptedException;

    @Nullable
    SlNode selectOrder() throws InterruptedException;

    @Nullable
    SlNode selectTrade() throws InterruptedException;

    @Nullable
    SlNode selectPosn(int busDay) throws InterruptedException;
}

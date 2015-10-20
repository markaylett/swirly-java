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

public interface Model extends AutoCloseable {

    @NonNull
    MnemRbTree selectAsset(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    MnemRbTree selectContr(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    MnemRbTree selectMarket(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    MnemRbTree selectTrader(@NonNull Factory factory) throws InterruptedException;

    @Nullable
    String selectTraderByEmail(@NonNull String email, @NonNull Factory factory)
            throws InterruptedException;

    @NonNull
    MnemRbTree selectView(@NonNull Factory factory) throws InterruptedException;

    @Nullable
    SlNode selectOrder(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    InstructTree selectOrder(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException;

    @Nullable
    SlNode selectTrade(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    InstructTree selectTrade(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException;

    @Nullable
    SlNode selectPosn(int busDay, @NonNull Factory factory) throws InterruptedException;

    @NonNull
    TraderPosnTree selectPosn(@NonNull String trader, int busDay, @NonNull Factory factory)
            throws InterruptedException;

    long selectTimeout() throws InterruptedException;
}

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
    MnemRbTree readAsset(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    MnemRbTree readContr(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    MnemRbTree readMarket(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    MnemRbTree readTrader(@NonNull Factory factory) throws InterruptedException;

    @Nullable
    String readTraderByEmail(@NonNull String email, @NonNull Factory factory)
            throws InterruptedException;

    @NonNull
    MnemRbTree readView(@NonNull Factory factory) throws InterruptedException;

    @Nullable
    SlNode readOrder(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    InstructTree readOrder(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException;

    @Nullable
    SlNode readTrade(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    InstructTree readTrade(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException;

    @Nullable
    SlNode readPosn(int busDay, @NonNull Factory factory) throws InterruptedException;

    @NonNull
    TraderPosnTree readPosn(@NonNull String trader, int busDay, @NonNull Factory factory)
            throws InterruptedException;
}

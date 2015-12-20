/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.io;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.entity.Factory;
import com.swirlycloud.swirly.entity.MarketViewTree;
import com.swirlycloud.swirly.entity.RecTree;
import com.swirlycloud.swirly.entity.RequestIdTree;
import com.swirlycloud.swirly.entity.TraderPosnTree;
import com.swirlycloud.swirly.node.SlNode;

public interface Model extends AutoCloseable {

    @NonNull
    RecTree readAsset(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    RecTree readContr(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    RecTree readMarket(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    RecTree readTrader(@NonNull Factory factory) throws InterruptedException;

    @Nullable
    String readTraderByEmail(@NonNull String email, @NonNull Factory factory)
            throws InterruptedException;

    @NonNull
    MarketViewTree readView(@NonNull Factory factory) throws InterruptedException;

    @Nullable
    SlNode readOrder(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    RequestIdTree readOrder(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException;

    @Nullable
    SlNode readTrade(@NonNull Factory factory) throws InterruptedException;

    @NonNull
    RequestIdTree readTrade(@NonNull String trader, @NonNull Factory factory)
            throws InterruptedException;

    @Nullable
    SlNode readPosn(int busDay, @NonNull Factory factory) throws InterruptedException;

    @NonNull
    TraderPosnTree readPosn(@NonNull String trader, int busDay, @NonNull Factory factory)
            throws InterruptedException;
}

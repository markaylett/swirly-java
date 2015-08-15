/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.node.SlNode;

public interface Model extends AutoCloseable {

    @Nullable
    MnemRbTree selectAsset();

    @Nullable
    MnemRbTree selectContr();

    @Nullable
    MnemRbTree selectMarket();

    @Nullable
    MnemRbTree selectTrader();

    @Nullable
    SlNode selectOrder();

    @Nullable
    SlNode selectTrade();

    @Nullable
    SlNode selectPosn(int busDay);
}

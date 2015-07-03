/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.SlNode;

public interface Model extends Journ {

    @Nullable
    SlNode selectAsset();

    @Nullable
    SlNode selectContr();

    @Nullable
    SlNode selectMarket();

    @Nullable
    SlNode selectTrader();

    @Nullable
    SlNode selectOrder();

    @Nullable
    SlNode selectTrade();

    @Nullable
    SlNode selectPosn();
}

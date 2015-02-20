/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import com.swirlycloud.twirly.node.SlNode;

public interface Model extends Journ {

    SlNode selectAsset();

    SlNode selectContr();

    SlNode selectMarket();

    SlNode selectTrader();

    SlNode selectOrder();

    SlNode selectTrade();

    SlNode selectPosn();
}

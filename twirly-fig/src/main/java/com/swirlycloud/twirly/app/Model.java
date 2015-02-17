/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.SlNode;

public interface Model extends Journ {

    void insertMarket(Market market);

    void insertTrader(Trader trader);

    void archiveOrder(String market, long id, long modified) throws NotFoundException;

    void archiveTrade(String market, long id, long modified) throws NotFoundException;

    SlNode selectAsset();

    SlNode selectContr();

    SlNode selectMarket();

    SlNode selectTrader();

    SlNode selectOrder();

    SlNode selectTrade();

    SlNode selectPosn();
}

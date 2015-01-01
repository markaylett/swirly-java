/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.function.UnaryCallback;

public interface Model extends Journ {

    /**
     * Allocate a "global" trader-id.
     * 
     * @return the newly allocated id.
     */
    long allocTraderId();

    void insertTrader(Trader trader);

    void insertMarket(long contrId, int settlDay, int fixingDay, int expiryDay);

    void archiveOrder(long contrId, int settlDay, long id, long modified) throws NotFoundException;

    void archiveTrade(long contrId, int settlDay, long id, long modified) throws NotFoundException;

    void selectAsset(UnaryCallback<Asset> cb);

    void selectContr(UnaryCallback<Contr> cb);

    void selectTrader(UnaryCallback<Trader> cb);

    void selectMarket(UnaryCallback<Market> cb);

    void selectOrder(UnaryCallback<Order> cb);

    void selectTrade(UnaryCallback<Exec> cb);

    void selectPosn(UnaryCallback<Posn> cb);
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Trader;
import com.swirlycloud.exception.NotFoundException;
import com.swirlycloud.function.UnaryCallback;

public interface Model extends Journ {

    /**
     * Allocate a "global" trader-id.
     * 
     * @return the newly allocated id.
     */
    long allocTraderId();

    void insertTrader(Trader trader);

    void insertMarket(long contrId, int settlDay, int expiryDay);

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

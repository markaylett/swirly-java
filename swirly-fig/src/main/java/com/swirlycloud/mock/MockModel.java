/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.mock;

import com.swirlycloud.collection.SlNode;
import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Trader;
import com.swirlycloud.engine.Model;
import com.swirlycloud.function.UnaryCallback;

public final class MockModel implements Model {
    private long maxTraderId = 0L;

    @Override
    public final void insertExec(long contrId, int settlDay, Exec exec) {
    }

    @Override
    public final void insertExecList(long contrId, int settlDay, SlNode first) {
    }

    @Override
    public final long allocTraderId() {
        return ++maxTraderId;
    }

    @Override
    public final void insertTrader(Trader trader) {
    }

    @Override
    public final void insertMarket(long contrId, int settlDay, int expiryDay) {
    }

    @Override
    public final void archiveOrder(long contrId, int settlDay, long id, long modified) {
    }

    @Override
    public final void archiveTrade(long contrId, int settlDay, long id, long modified) {
    }

    @Override
    public final void selectAsset(UnaryCallback<Asset> cb) {
        MockAsset.selectAsset(cb);
    }

    @Override
    public final void selectContr(UnaryCallback<Contr> cb) {
        MockContr.selectContr(cb);
    }

    @Override
    public final void selectTrader(UnaryCallback<Trader> cb) {
        MockTrader.selectTrader(cb);
    }

    @Override
    public final void selectMarket(UnaryCallback<Market> cb) {
    }

    @Override
    public final void selectOrder(UnaryCallback<Order> cb) {
    }

    @Override
    public final void selectTrade(UnaryCallback<Exec> cb) {
    }

    @Override
    public final void selectPosn(UnaryCallback<Posn> cb) {
    }
}

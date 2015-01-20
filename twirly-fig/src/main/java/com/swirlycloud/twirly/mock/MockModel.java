/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import com.swirlycloud.twirly.app.Model;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.SlNode;

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
    public final void insertMarket(long contrId, int settlDay, int fixingDay, int expiryDay) {
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

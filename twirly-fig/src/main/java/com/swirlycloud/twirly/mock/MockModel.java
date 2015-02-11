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
import com.swirlycloud.twirly.node.SlNode;

public final class MockModel implements Model {
    
    @Override
    public final void close() {
    }

    @Override
    public final void insertExec(Exec exec) {
    }

    @Override
    public final void insertExecList(String market, SlNode first) {
    }

    @Override
    public final void insertMarket(Market market) {
    }

    @Override
    public final void insertTrader(Trader trader) {
    }

    @Override
    public final void archiveOrder(String market, long id, long modified) {
    }

    @Override
    public final void archiveTrade(String market, long id, long modified) {
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
    public final void selectMarket(UnaryCallback<Market> cb) {
    }

    @Override
    public final void selectTrader(UnaryCallback<Trader> cb) {
        MockTrader.selectTrader(cb);
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

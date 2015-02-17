/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import com.swirlycloud.twirly.app.Model;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Trader;
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
    public final SlNode selectAsset() {
        return MockAsset.selectAsset();
    }

    @Override
    public final SlNode selectContr() {
        return MockContr.selectContr();
    }

    @Override
    public final SlNode selectMarket() {
        return null;
    }

    @Override
    public final SlNode selectTrader() {
        return MockTrader.selectTrader();
    }

    @Override
    public final SlNode selectOrder() {
        return null;
    }

    @Override
    public final SlNode selectTrade() {
        return null;
    }

    @Override
    public final SlNode selectPosn() {
        return null;
    }
}

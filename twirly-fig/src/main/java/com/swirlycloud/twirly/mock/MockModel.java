/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.SlNode;

public class MockModel implements Model {

    @Override
    public void close() {
    }

    @Override
    public void insertMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state) {
    }

    @Override
    public void updateMarket(String mnem, String display, int state) {
    }

    @Override
    public void insertTrader(String mnem, String display, String email) {
    }

    @Override
    public void updateTrader(String mnem, String display) {
    }

    @Override
    public void insertExec(Exec exec) {
    }

    @Override
    public void insertExecList(String market, SlNode first) {
    }

    @Override
    public void archiveOrder(String market, long id, long modified) {
    }

    @Override
    public void archiveTrade(String market, long id, long modified) {
    }

    @Override
    public SlNode selectAsset() {
        return MockAsset.selectAsset();
    }

    @Override
    public SlNode selectContr() {
        return MockContr.selectContr();
    }

    @Override
    public SlNode selectMarket() {
        return null;
    }

    @Override
    public SlNode selectTrader() {
        return MockTrader.selectTrader();
    }

    @Override
    public SlNode selectOrder() {
        return null;
    }

    @Override
    public SlNode selectTrade() {
        return null;
    }

    @Override
    public SlNode selectPosn() {
        return null;
    }
}

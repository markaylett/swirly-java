/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.SlNode;

public class MockModel implements Model {

    @Override
    public void close() {
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
    public SlNode selectPosn(int busDay) {
        return null;
    }
}

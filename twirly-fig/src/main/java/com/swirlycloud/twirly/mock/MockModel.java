/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.SlNode;

public class MockModel implements Model {

    @Override
    public void close() {
    }

    @Override
    public @NonNull MnemRbTree selectAsset() {
        return MockAsset.selectAsset();
    }

    @Override
    public MnemRbTree selectContr() {
        return MockContr.selectContr();
    }

    @Override
    public MnemRbTree selectMarket() {
        return new MnemRbTree();
    }

    @Override
    public MnemRbTree selectTrader() {
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.SlNode;

public class MockModel implements Model {

    private final MockAsset mockAsset;
    private final MockContr mockContr;
    private final MockTrader mockTrader;

    public MockModel(Factory factory) {
        mockAsset = new MockAsset(factory);
        mockContr = new MockContr(factory);
        mockTrader = new MockTrader(factory);
    }

    @Override
    public void close() {
    }

    @Override
    public @NonNull MnemRbTree selectAsset() {
        return mockAsset.selectAsset();
    }

    @Override
    public MnemRbTree selectContr() {
        return mockContr.selectContr();
    }

    @Override
    public MnemRbTree selectMarket() {
        return new MnemRbTree();
    }

    @Override
    public MnemRbTree selectTrader() {
        return mockTrader.selectTrader();
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

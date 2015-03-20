/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.concurrent.Future;

import com.swirlycloud.twirly.node.SlNode;

public interface AsyncModel extends Journ {

    Future<SlNode> selectAsset();

    Future<SlNode> selectContr();

    Future<SlNode> selectMarket();

    Future<SlNode> selectTrader();

    Future<SlNode> selectOrder();

    Future<SlNode> selectTrade();

    Future<SlNode> selectPosn();
}

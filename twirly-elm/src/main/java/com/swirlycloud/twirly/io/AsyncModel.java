/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.node.SlNode;

public interface AsyncModel extends Journ {

    @NonNull
    Future<SlNode> selectAsset();

    @NonNull
    Future<SlNode> selectContr();

    @NonNull
    Future<SlNode> selectMarket();

    @NonNull
    Future<SlNode> selectTrader();

    @NonNull
    Future<SlNode> selectOrder();

    @NonNull
    Future<SlNode> selectTrade();

    @NonNull
    Future<SlNode> selectPosn();
}

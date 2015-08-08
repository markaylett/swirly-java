/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.node.SlNode;

public interface AsyncModel {

    @NonNull
    Future<MnemRbTree> selectAsset();

    @NonNull
    Future<MnemRbTree> selectContr();

    @NonNull
    Future<MnemRbTree> selectMarket();

    @NonNull
    Future<MnemRbTree> selectTrader();

    @NonNull
    Future<SlNode> selectOrder();

    @NonNull
    Future<SlNode> selectTrade();

    @NonNull
    Future<SlNode> selectPosn(int busDay);
}

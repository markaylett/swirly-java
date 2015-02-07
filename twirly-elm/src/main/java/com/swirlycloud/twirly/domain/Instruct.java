/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Identifiable;

/**
 * Fields common to both Order and Exec.
 */

public interface Instruct extends RbNode, Identifiable, Financial {

    long getOrderId();

    String getTrader();

    @Override
    String getMarket();

    @Override
    String getContr();

    @Override
    int getSettlDay();

    String getRef();

    State getState();

    Action getAction();

    long getTicks();

    long getLots();

    long getResd();

    long getExec();

    long getLastTicks();

    long getLastLots();

    long getMinLots();

    boolean isDone();
}

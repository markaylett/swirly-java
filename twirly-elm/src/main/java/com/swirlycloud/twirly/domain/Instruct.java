/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

/**
 * Fields common to both Order and Exec.
 */

public interface Instruct {

    long getOrderId();

    long getTraderId();

    Trader getTrader();

    long getContrId();

    Contr getContr();

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

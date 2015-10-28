/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Fields common to both Order and Exec.
 * 
 * @author Mark Aylett
 */
public @NonNullByDefault interface Instruct extends Request {

    @Override
    long getId();

    /**
     * @return order-id or zero if there is no associated order.
     */
    long getOrderId();

    @Override
    String getTrader();

    @Override
    String getMarket();

    @Override
    String getContr();

    @Override
    int getSettlDay();

    @Override
    boolean isSettlDaySet();

    @Override
    @Nullable
    String getRef();

    State getState();

    @Override
    Side getSide();

    long getTicks();

    @Override
    long getLots();

    long getResd();

    /**
     * @return sum of lots traded.
     */
    long getExec();

    /**
     * @return sum of lastLots*lastTicks for each trade.
     */
    long getCost();

    /**
     * @return cost/exec or zero if exec is zero.
     */
    double getAvgTicks();

    /**
     * @return last traded ticks.
     */
    long getLastTicks();

    /**
     * @return last traded lots.
     */
    long getLastLots();

    long getMinLots();

    /**
     * @return true if resd equals zero.
     */
    boolean isDone();

    @Override
    long getCreated();
}

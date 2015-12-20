/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.domain.State;

/**
 * Fields common to both Order and Exec.
 * 
 * @author Mark Aylett
 */
public @NonNullByDefault interface Instruct extends Request {

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
    long getId();

    @Override
    @Nullable
    String getRef();

    /**
     * @return order-id or zero if there is no associated order.
     */
    long getOrderId();

    /**
     * @return quote-id or zero if there is no associated quote.
     */
    long getQuoteId();

    State getState();

    @Override
    Side getSide();

    @Override
    long getLots();

    long getTicks();

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
     * @return last traded lots.
     */
    long getLastLots();

    /**
     * @return last traded ticks.
     */
    long getLastTicks();

    long getMinLots();

    /**
     * @return true if resd equals zero.
     */
    boolean isDone();

    @Override
    long getCreated();
}

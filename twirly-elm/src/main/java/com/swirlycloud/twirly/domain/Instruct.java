/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Identifiable;

/**
 * Fields common to both Order and Exec.
 */

/**
 * @author Mark Aylett
 *
 */
public @NonNullByDefault interface Instruct extends RbNode, Identifiable, Financial {

    /**
     * @return order-id or zero if there is no associated order.
     */
    long getOrderId();

    String getTrader();

    @Override
    String getMarket();

    @Override
    String getContr();

    @Override
    int getSettlDay();

    @Nullable
    String getRef();

    State getState();

    Action getAction();

    long getTicks();

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
}

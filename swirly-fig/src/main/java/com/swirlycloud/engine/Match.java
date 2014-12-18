/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.collection.BasicSlNode;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;

/**
 * A Match represents two orders from opposing sides of the market that may trade.
 */

public final class Match extends BasicSlNode {
    long ticks;
    long lots;
    Order makerOrder;
    Exec makerTrade;
    Posn makerPosn;
    Exec takerTrade;

    public final long getTicks() {
        return ticks;
    }

    public final long getLots() {
        return lots;
    }

    public final Order getMakerOrder() {
        return makerOrder;
    }

    public final Exec getMakerExec() {
        return makerTrade;
    }

    public final Posn getMakerPosn() {
        return makerPosn;
    }

    public final Exec getTakerExec() {
        return takerTrade;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.app;

import com.swirlycloud.swirly.entity.Exec;
import com.swirlycloud.swirly.entity.Order;
import com.swirlycloud.swirly.entity.Posn;
import com.swirlycloud.swirly.node.AbstractSlNode;

/**
 * A Match represents two orders from opposing sides of the market that may trade.
 * 
 * @author Mark Aylett
 */
final class Match extends AbstractSlNode {
    long lots;
    long ticks;
    Order makerOrder;
    Exec makerTrade;
    Posn makerPosn;
    Exec takerTrade;

    public final long getLots() {
        return lots;
    }

    public final long getTicks() {
        return ticks;
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import com.swirlycloud.twirly.entity.Exec;
import com.swirlycloud.twirly.entity.Order;
import com.swirlycloud.twirly.entity.Posn;
import com.swirlycloud.twirly.node.AbstractSlNode;

/**
 * A Match represents two orders from opposing sides of the market that may trade.
 * 
 * @author Mark Aylett
 */
public final class Match extends AbstractSlNode {
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

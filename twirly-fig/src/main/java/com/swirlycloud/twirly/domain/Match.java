/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.node.BasicSlNode;

/**
 * A Match represents two orders from opposing sides of the market that may trade.
 * 
 * @author Mark Aylett
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

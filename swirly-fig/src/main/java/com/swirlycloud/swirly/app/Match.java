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
    final long lots;
    final Order makerOrder;
    final Exec makerTrade;
    final Posn makerPosn;
    final Exec takerTrade;

    Match(long lots, Order makerOrder, Exec makerTrade, Posn makerPosn,
            Exec takerTrade) {
        this.lots = lots;
        this.makerOrder = makerOrder;
        this.makerTrade = makerTrade;
        this.makerPosn = makerPosn;
        this.takerTrade = takerTrade;
    }
}

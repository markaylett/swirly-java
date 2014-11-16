/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Exec;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.util.BasicSlNode;

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

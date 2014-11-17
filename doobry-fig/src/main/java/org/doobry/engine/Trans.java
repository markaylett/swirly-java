/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Exec;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.util.Queue;

public final class Trans {
    final Order newOrder;
    final Queue matches;
    /**
     * All executions referenced in matches.
     */
    final Queue execs;
    Posn takerPosn;

    public Trans(Order newOrder, Exec newExec) {
        this.newOrder = newOrder;
        matches = new Queue();
        execs = new Queue();
        takerPosn = null;
        execs.insertBack(newExec);
    }
}

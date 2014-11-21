/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import java.util.List;

import org.doobry.domain.Exec;
import org.doobry.domain.Kind;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;

public interface Model {

    /**
     * Allocate contiguous sequence of identifiers and return the first.
     */
    long allocIds(Kind kind, long num);

    void insertExecList(Exec first);

    void insertExec(Exec exec);

    void updateExec(long id, long modified);

    Rec getRecList(Kind kind);

    List<Order> getOrders();

    List<Exec> getTrades();

    List<Posn> getPosns();
}

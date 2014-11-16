/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import java.util.Collection;

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

    Rec readRec(Kind kind);

    Collection<Order> readOrder();

    Collection<Exec> readTrade();

    Collection<Posn> readPosn();
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import java.util.List;

import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Rec;

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

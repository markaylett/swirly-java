/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.User;
import com.swirlycloud.function.UnaryCallback;

public interface Model {

    /**
     * Allocate contiguous sequence of identifiers and return the first.
     */
    long allocUserIds(long num);

    /**
     * Allocate contiguous sequence of identifiers and return the first.
     */
    long allocOrderIds(long num);

    /**
     * Allocate contiguous sequence of identifiers and return the first.
     */
    long allocExecIds(long num);

    void insertUser(User user);

    void insertExecList(long contrId, int settlDay, Exec first);

    void insertExec(long contrId, int settlDay, Exec exec);

    void updateExec(long contrId, int settlDay, long id, long modified);

    void selectAsset(UnaryCallback<Asset> cb);

    void selectContr(UnaryCallback<Contr> cb);

    void selectUser(UnaryCallback<User> cb);

    void selectMarket(UnaryCallback<Market> cb);

    void selectOrder(UnaryCallback<Order> cb);

    void selectTrade(UnaryCallback<Exec> cb);

    void selectPosn(UnaryCallback<Posn> cb);
}

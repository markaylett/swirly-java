/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.User;
import com.swirlycloud.function.UnaryCallback;

public interface Model {

    /**
     * Allocate contiguous sequence of identifiers and return the first.
     */
    long allocIds(Kind kind, long num);

    void insertUser(User user);

    void insertExecList(Exec first);

    void insertExec(Exec exec);

    void updateExec(long id, long modified);

    void selectAsset(UnaryCallback<Asset> cb);

    void selectContr(UnaryCallback<Contr> cb);

    void selectUser(UnaryCallback<User> cb);

    void selectOrder(UnaryCallback<Order> cb);

    void selectTrade(UnaryCallback<Exec> cb);

    void selectPosn(UnaryCallback<Posn> cb);
}

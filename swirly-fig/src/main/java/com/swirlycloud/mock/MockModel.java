/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.mock;

import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.User;
import com.swirlycloud.engine.Model;
import com.swirlycloud.function.UnaryCallback;

public final class MockModel implements Model {
    private long maxUserId = 0L;

    @Override
    public final void insertExecList(long contrId, int settlDay, Exec first) {
    }

    @Override
    public final void insertExec(long contrId, int settlDay, Exec exec) {
    }

    @Override
    public final void updateExec(long contrId, int settlDay, long id, long modified) {
    }

    @Override
    public final long allocUserId() {
        return ++maxUserId;
    }

    @Override
    public final void insertUser(User user) {
    }

    @Override
    public final void selectAsset(UnaryCallback<Asset> cb) {
        MockAsset.selectAsset(cb);
    }

    @Override
    public final void selectContr(UnaryCallback<Contr> cb) {
        MockContr.selectContr(cb);
    }

    @Override
    public final void selectUser(UnaryCallback<User> cb) {
        MockUser.selectUser(cb);
    }

    @Override
    public final void selectMarket(UnaryCallback<Market> cb) {
    }

    @Override
    public final void selectOrder(UnaryCallback<Order> cb) {
    }

    @Override
    public final void selectTrade(UnaryCallback<Exec> cb) {
    }

    @Override
    public final void selectPosn(UnaryCallback<Posn> cb) {
    }
}

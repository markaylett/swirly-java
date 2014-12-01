/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.mock;

import java.util.Arrays;

import com.swirlycloud.domain.Asset;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.User;
import com.swirlycloud.engine.Model;
import com.swirlycloud.function.UnaryCallback;

public final class MockModel implements Model {
    private final long[] arr;

    public MockModel() {
        arr = new long[Kind.values().length];
        Arrays.fill(arr, 1);
    }

    @Override
    public final long allocIds(Kind kind, long num) {
        final long nextId = arr[kind.ordinal()];
        arr[kind.ordinal()] += num;
        return nextId;
    }

    @Override
    public final void insertUser(User user) {
    }

    @Override
    public final void insertExecList(long bookId, Exec first) {
    }

    @Override
    public final void insertExec(long bookId, Exec exec) {
    }

    @Override
    public final void updateExec(long bookId, long id, long modified) {
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
    public final void selectOrder(UnaryCallback<Order> cb) {
    }

    @Override
    public final void selectTrade(UnaryCallback<Exec> cb) {
    }

    @Override
    public final void selectPosn(UnaryCallback<Posn> cb) {
    }
}

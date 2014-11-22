/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.engine.Model;
import com.swirlycloud.mock.MockAsset;
import com.swirlycloud.mock.MockContr;
import com.swirlycloud.mock.MockUser;

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
    public final void insertExecList(Exec first) {
    }

    @Override
    public final void insertExec(Exec exec) {
    }

    @Override
    public final void updateExec(long id, long modified) {
    }

    @Override
    public final Rec getRecList(Kind kind) {
        Rec first = null;
        switch (kind) {
        case ASSET:
            first = MockAsset.newAssetList();
            break;
        case CONTR:
            first = MockContr.newContrList();
            break;
        case USER:
            first = MockUser.newUserList();
            break;
        default:
            throw new IllegalArgumentException("invalid record-type");
        }
        return first;
    }

    @Override
    public final List<Order> getOrders() {
        return Collections.emptyList();
    }

    @Override
    public final List<Exec> getTrades() {
        return Collections.emptyList();
    }

    @Override
    public final List<Posn> getPosns() {
        return Collections.emptyList();
    }
}

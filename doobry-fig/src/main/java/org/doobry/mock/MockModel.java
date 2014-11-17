/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.doobry.domain.Exec;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.domain.Kind;
import org.doobry.engine.Model;

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
    public final Rec selectRec(Kind kind) {
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
    public final Collection<Order> selectOrder() {
        return Collections.emptyList();
    }

    @Override
    public final Collection<Exec> selectTrade() {
        return Collections.emptyList();
    }

    @Override
    public final Collection<Posn> selectPosn() {
        return Collections.emptyList();
    }
}

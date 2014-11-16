/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import org.doobry.domain.Exec;
import org.doobry.domain.Model;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.domain.Kind;

public final class MockModel implements Model {

    @Override
    public final Rec readRec(Kind kind) {
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
    public final Order[] readOrder() {
        return new Order[] {};
    }

    @Override
    public final Exec[] readTrade() {
        return new Exec[] {};
    }

    @Override
    public final Posn[] readPosn() {
        return new Posn[] {};
    }
}

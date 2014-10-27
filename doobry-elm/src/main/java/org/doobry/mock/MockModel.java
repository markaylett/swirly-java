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
import org.doobry.domain.RecType;

public final class MockModel implements Model {

    @Override
    public final Rec[] readRec(RecType type) {
        Rec[] arr = null;
        switch (type) {
        case ASSET:
            arr = MockAsset.newAssetArray();
            break;
        case CONTR:
            arr = MockContr.newContrArray();
            break;
        case PARTY:
            arr = MockParty.newPartyArray();
            break;
        }
        return arr;
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

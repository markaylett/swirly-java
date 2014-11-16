/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import java.util.Collection;
import java.util.Collections;

import org.doobry.domain.Exec;
import org.doobry.domain.Kind;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.engine.Model;
import org.doobry.mock.MockAsset;
import org.doobry.mock.MockContr;
import org.doobry.mock.MockUser;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyRange;

public final class DatastoreModel implements Model {

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public final long allocIds(Kind kind, long num) {
        final KeyRange range = datastore.allocateIds(kind.camelName(), num);
        return range.getStart().getId();
    }

    @Override
    public final void insertExecList(Exec first) {
        // TODO Auto-generated method stub
    }

    @Override
    public final void insertExec(Exec exec) {
        // TODO Auto-generated method stub
    }

    @Override
    public final void updateExec(long id, long modified) {
        // TODO Auto-generated method stub
    }

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
    public final Collection<Order> readOrder() {
        return Collections.emptyList();
    }

    @Override
    public final Collection<Exec> readTrade() {
        return Collections.emptyList();
    }

    @Override
    public final Collection<Posn> readPosn() {
        return Collections.emptyList();
    }
}

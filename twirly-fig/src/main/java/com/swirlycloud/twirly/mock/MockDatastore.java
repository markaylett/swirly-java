/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.node.SlNode;

public class MockDatastore extends MockModel implements Datastore {

    @Override
    public void insertMarket(@NonNull String mnem, @Nullable String display, @NonNull String contr,
            int settlDay, int expiryDay, int state) {
    }

    @Override
    public void updateMarket(@NonNull String mnem, @Nullable String display, int state) {
    }

    @Override
    public void insertTrader(@NonNull String mnem, @Nullable String display, @NonNull String email) {
    }

    @Override
    public void updateTrader(@NonNull String mnem, @Nullable String display)
            throws NotFoundException {
    }

    @Override
    public void insertExec(@NonNull Exec exec) {
    }

    @Override
    public void insertExecList(@NonNull String market, @NonNull SlNode first) {
    }

    @Override
    public void insertExecList(@NonNull SlNode first) {
    }

    @Override
    public void archiveOrder(@NonNull String market, long id, long modified) {
    }

    @Override
    public void archiveTrade(@NonNull String market, long id, long modified) {
    }
}

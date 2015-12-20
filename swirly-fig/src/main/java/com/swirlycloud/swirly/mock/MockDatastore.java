/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.entity.Exec;
import com.swirlycloud.swirly.entity.Quote;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.io.Datastore;
import com.swirlycloud.swirly.node.JslNode;

public class MockDatastore extends MockModel implements Datastore {

    @Override
    public void createMarket(@NonNull String mnem, @Nullable String display, @NonNull String contr,
            int settlDay, int expiryDay, int state) {
    }

    @Override
    public void updateMarket(@NonNull String mnem, @Nullable String display, int state) {
    }

    @Override
    public void createTrader(@NonNull String mnem, @Nullable String display,
            @NonNull String email) {
    }

    @Override
    public void updateTrader(@NonNull String mnem, @Nullable String display)
            throws NotFoundException {
    }

    @Override
    public void createExec(@NonNull Exec exec) {
    }

    @Override
    public void createExecList(@NonNull String market, @NonNull JslNode first) {
    }

    @Override
    public void createExecList(@NonNull JslNode first) {
    }

    @Override
    public final void createQuote(@NonNull Quote quote) throws NotFoundException {
    }

    @Override
    public void archiveOrder(@NonNull String market, long id, long modified) {
    }

    @Override
    public void archiveOrderList(@NonNull String market, @NonNull JslNode first, long modified)
            throws NotFoundException {
    }

    @Override
    public void archiveOrderList(@NonNull JslNode first, long modified) throws NotFoundException {
    }

    @Override
    public void archiveTrade(@NonNull String market, long id, long modified) {
    }

    @Override
    public void archiveTradeList(@NonNull String market, @NonNull JslNode first, long modified)
            throws NotFoundException {
    }

    @Override
    public void archiveTradeList(@NonNull JslNode first, long modified) throws NotFoundException {
    }
}

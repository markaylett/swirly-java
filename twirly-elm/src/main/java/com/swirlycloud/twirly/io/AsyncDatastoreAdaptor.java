/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.concurrent.FutureAdapter;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.node.SlNode;

public final class AsyncDatastoreAdaptor implements AsyncDatastore {
    private final Datastore datastore;

    public AsyncDatastoreAdaptor(Datastore datastore) {
        this.datastore = datastore;
    }

    @Override
    public final void close() throws Exception {
        datastore.close();
    }

    @Override
    public final @NonNull Future<MnemRbTree> selectAsset() {
        return new FutureAdapter<>(datastore.selectAsset());
    }

    @Override
    public final @NonNull Future<MnemRbTree> selectContr() {
        return new FutureAdapter<>(datastore.selectContr());
    }

    @Override
    public final @NonNull Future<MnemRbTree> selectMarket() {
        return new FutureAdapter<>(datastore.selectMarket());
    }

    @Override
    public final @NonNull Future<MnemRbTree> selectTrader() {
        return new FutureAdapter<>(datastore.selectTrader());
    }

    @Override
    public final @NonNull Future<SlNode> selectOrder() {
        return new FutureAdapter<>(datastore.selectOrder());
    }

    @Override
    public final @NonNull Future<SlNode> selectTrade() {
        return new FutureAdapter<>(datastore.selectTrade());
    }

    @Override
    public final @NonNull Future<SlNode> selectPosn(int busDay) {
        return new FutureAdapter<>(datastore.selectPosn(busDay));
    }

    @Override
    public final void insertMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state) {
        datastore.insertMarket(mnem, display, contr, settlDay, expiryDay, state);
    }

    @Override
    public final void updateMarket(String mnem, String display, int state) throws NotFoundException {
        datastore.updateMarket(mnem, display, state);
    }

    @Override
    public final void insertTrader(String mnem, String display, String email) {
        datastore.insertTrader(mnem, display, email);
    }

    @Override
    public final void updateTrader(String mnem, String display) throws NotFoundException {
        datastore.updateTrader(mnem, display);
    }

    @Override
    public final void insertExec(Exec exec) throws NotFoundException {
        datastore.insertExec(exec);
    }

    @Override
    public final void insertExecList(String market, SlNode first) throws NotFoundException {
        datastore.insertExecList(market, first);
    }

    @Override
    public final void archiveOrder(String market, long id, long modified) throws NotFoundException {
        datastore.archiveOrder(market, id, modified);
    }

    @Override
    public final void archiveTrade(String market, long id, long modified) throws NotFoundException {
        datastore.archiveTrade(market, id, modified);
    }
}

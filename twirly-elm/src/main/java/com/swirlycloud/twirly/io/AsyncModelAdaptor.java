/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.concurrent.Future;

import com.swirlycloud.twirly.concurrent.FutureAdapter;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.SlNode;

public final class AsyncModelAdaptor implements AsyncModel {
    private final Model model;

    public AsyncModelAdaptor(Model model) {
        this.model = model;
    }

    @Override
    public final void close() throws Exception {
        model.close();
    }

    @Override
    public final void insertMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state) {
        model.insertMarket(mnem, display, contr, settlDay, expiryDay, state);
    }

    @Override
    public final void updateMarket(String mnem, String display, int state) throws NotFoundException {
        model.updateMarket(mnem, display, state);
    }

    @Override
    public final void insertTrader(String mnem, String display, String email) {
        model.insertTrader(mnem, display, email);
    }

    @Override
    public final void updateTrader(String mnem, String display) throws NotFoundException {
        model.updateTrader(mnem, display);
    }

    @Override
    public final void insertExec(Exec exec) throws NotFoundException {
        model.insertExec(exec);
    }

    @Override
    public final void insertExecList(String market, SlNode first) throws NotFoundException {
        model.insertExecList(market, first);
    }

    @Override
    public final void archiveOrder(String market, long id, long modified) throws NotFoundException {
        model.archiveOrder(market, id, modified);
    }

    @Override
    public final void archiveTrade(String market, long id, long modified) throws NotFoundException {
        model.archiveTrade(market, id, modified);
    }

    @Override
    public final Future<SlNode> selectAsset() {
        return new FutureAdapter<>(model.selectAsset());
    }

    @Override
    public final Future<SlNode> selectContr() {
        return new FutureAdapter<>(model.selectContr());
    }

    @Override
    public final Future<SlNode> selectMarket() {
        return new FutureAdapter<>(model.selectMarket());
    }

    @Override
    public final Future<SlNode> selectTrader() {
        return new FutureAdapter<>(model.selectTrader());
    }

    @Override
    public final Future<SlNode> selectOrder() {
        return new FutureAdapter<>(model.selectOrder());
    }

    @Override
    public final Future<SlNode> selectTrade() {
        return new FutureAdapter<>(model.selectTrade());
    }

    @Override
    public final Future<SlNode> selectPosn() {
        return new FutureAdapter<>(model.selectPosn());
    }
}

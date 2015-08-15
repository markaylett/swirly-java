/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.node.SlNode;

public class MockDatastore extends MockModel implements Datastore {

    public MockDatastore(Factory factory) {
        super(factory);
    }

    @Override
    public void insertMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state) {
    }

    @Override
    public void updateMarket(String mnem, String display, int state) {
    }

    @Override
    public void insertTrader(String mnem, String display, String email) {
    }

    @Override
    public void updateTrader(String mnem, String display) throws NotFoundException {
    }

    @Override
    public void insertExec(Exec exec) {
    }

    @Override
    public void insertExecList(String market, SlNode first) {
    }

    @Override
    public void archiveOrder(String market, long id, long modified) {
    }

    @Override
    public void archiveTrade(String market, long id, long modified) {
    }
}

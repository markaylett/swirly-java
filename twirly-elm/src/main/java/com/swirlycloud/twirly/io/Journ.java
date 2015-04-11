/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.SlNode;

/**
 * Transaction journal.
 * 
 * @author Mark Aylett
 */
public interface Journ extends AutoCloseable {

    void insertMarket(String mnem, String display, String contr, int settlDay, int expiryDay,
            int state);

    void updateMarket(String mnem, String display, int state);

    void insertTrader(String mnem, String display, String email);

    void updateTrader(String mnem, String display);

    void insertExec(Exec exec) throws NotFoundException;

    void insertExecList(String market, SlNode first) throws NotFoundException;

    void archiveOrder(String market, long id, long modified) throws NotFoundException;

    void archiveTrade(String market, long id, long modified) throws NotFoundException;
}

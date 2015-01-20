/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.SlNode;

/**
 * Transaction journal.
 * 
 * @author Mark Aylett
 */
public interface Journ {

    void insertExec(long contrId, int settlDay, Exec exec) throws NotFoundException;

    void insertExecList(long contrId, int settlDay, SlNode first) throws NotFoundException;
}

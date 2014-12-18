/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.collection.SlNode;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.exception.NotFoundException;

/**
 * Transaction journal.
 * 
 * @author Mark Aylett
 */
public interface Journ {

    void insertExec(long contrId, int settlDay, Exec exec) throws NotFoundException;

    void insertExecList(long contrId, int settlDay, SlNode first) throws NotFoundException;
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.domain.Exec;

/**
 * Transaction journal.
 * 
 * @author Mark Aylett
 */
public interface Journ {

    void insertExec(long contrId, int settlDay, Exec exec);

    void insertExecList(long contrId, int settlDay, Exec first);
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.quickfix;

import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

public final class NullLogFactory implements LogFactory {
    private static final Log log = new NullLog();

    @SuppressWarnings("deprecation")
    @Override
    public final Log create() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Log create(SessionID sessionId) {
        return log;
    }
}

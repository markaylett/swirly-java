/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

public final class Slf4jLogFactory implements LogFactory {

    @SuppressWarnings("deprecation")
    @Override
    public final Log create() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Log create(SessionID sessionId) {
        return new Slf4jLog(sessionId);
    }
}

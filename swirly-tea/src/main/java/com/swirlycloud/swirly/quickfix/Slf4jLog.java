/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Log;
import quickfix.SessionID;

final class Slf4jLog implements Log {
    private final static Logger log = LoggerFactory.getLogger(Slf4jLog.class);

    private final SessionID sessionId;

    public Slf4jLog(SessionID sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public final void clear() {
    }

    @Override
    public final void onIncoming(String message) {
        if (log.isDebugEnabled()) {
            log.debug(sessionId + ": onIncoming: " + message);
        }
    }

    @Override
    public final void onOutgoing(String message) {
        if (log.isDebugEnabled()) {
            log.debug(sessionId + ": onOutgoing: " + message);
        }
    }

    @Override
    public final void onEvent(String text) {
        log.info(sessionId + ": onEvent: " + text);
    }

    @Override
    public final void onErrorEvent(String text) {
        log.error(sessionId + ": onErrorEvent: " + text);
    }
}

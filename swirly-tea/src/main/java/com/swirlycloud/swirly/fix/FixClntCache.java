/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.fix;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.swirly.exception.AlreadyExistsException;

import quickfix.Message;
import quickfix.SessionID;
import quickfix.SessionNotFound;

final @NonNullByDefault class FixClntCache {

    private final ConcurrentMap<SessionID, FixCache> cache = new ConcurrentHashMap<>();

    private final FixCache get(SessionID sessionId) {
        final FixCache sc = cache.get(sessionId);
        if (null != sc) {
            return sc;
        }
        // Lazy creation.
        final FixCache ours = new FixCache(sessionId);
        final FixCache theirs = cache.putIfAbsent(sessionId, ours);
        // Return winner of race to insert.
        return null == theirs ? ours : theirs;
    }

    final void clear(SessionID sessionId) {
        get(sessionId).clear();
    }

    final Future<Message> putRequest(String ref, int seqNum, SessionID sessionId)
            throws AlreadyExistsException {
        return get(sessionId).putRequest(ref, seqNum);
    }

    final Future<Message> sendRequest(String ref, Message message, SessionID sessionId)
            throws AlreadyExistsException, SessionNotFound {
        return get(sessionId).sendRequest(ref, message);
    }

    final void setResponse(String ref, Message message, SessionID sessionId) {
        get(sessionId).setResponse(ref, message);
    }

    final void setResponse(int seqNum, Message message, SessionID sessionId) {
        get(sessionId).setResponse(seqNum, message);
    }
}

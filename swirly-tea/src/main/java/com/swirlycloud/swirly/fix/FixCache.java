/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.fix;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.swirly.concurrent.FutureValue;
import com.swirlycloud.swirly.exception.AlreadyExistsException;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.MsgSeqNum;

final @NonNullByDefault class FixCache {

    private static final class Response extends FutureValue<Message> {
        // Used as key for WeakHashMap so object should not escape SessionCache.
        private final String ref;
        // Used as key for WeakHashMap so object should not escape SessionCache.
        private final Integer seqNum;

        private Response(String ref, int seqNum) {
            // Must be new objects.
            this.ref = new String(ref);
            this.seqNum = new Integer(seqNum);
        }
    }

    private final SessionID sessionId;
    @GuardedBy("this")
    private final WeakHashMap<String, WeakReference<Response>> refIdx = new WeakHashMap<>();
    @GuardedBy("this")
    private final WeakHashMap<Integer, WeakReference<Response>> seqNumIdx = new WeakHashMap<>();

    private final void put(Response res) {
        final WeakReference<Response> weakRef = new WeakReference<>(res);
        refIdx.put(res.ref, weakRef);
        seqNumIdx.put(res.seqNum, weakRef);
    }

    FixCache(SessionID sessionId) {
        this.sessionId = sessionId;
    }

    final void clear() {
        synchronized (this) {
            refIdx.clear();
            seqNumIdx.clear();
        }
    }

    // Unit testing.
    final Future<Message> putRequest(String ref, int seqNum) throws AlreadyExistsException {
        final Response res = new Response(ref, seqNum);
        synchronized (this) {
            if (refIdx.containsKey(ref)) {
                throw new AlreadyExistsException("duplicate ref: " + ref);
            }
            if (seqNumIdx.containsKey(seqNum)) {
                throw new AlreadyExistsException("duplicate seq-num: " + seqNum);
            }
            put(res);
        }
        return res;
    }

    final Future<Message> sendRequest(String ref, Message message)
            throws AlreadyExistsException, SessionNotFound {
        synchronized (this) {
            if (refIdx.containsKey(ref)) {
                throw new AlreadyExistsException("duplicate ref: " + ref);
            }
            // Assumption: seq-num cannot be duplicated.
            Session.sendToTarget(message, sessionId);
            try {
                final int seqNum = message.getHeader().getInt(MsgSeqNum.FIELD);
                final Response res = new Response(ref, seqNum);
                put(res);
                return res;
            } catch (final FieldNotFound e) {
                throw new RuntimeException("seq-num not found", e);
            }
        }
    }

    final void setResponse(String ref, Message message) {
        Response res = null;
        synchronized (this) {
            final WeakReference<Response> weakRef = refIdx.remove(ref);
            if (weakRef == null) {
                return;
            }
            res = weakRef.get();
            seqNumIdx.remove(res.seqNum);
        }
        res.set(message);
    }

    final void setResponse(int seqNum, Message message) {
        Response res = null;
        synchronized (this) {
            final WeakReference<Response> weakRef = seqNumIdx.remove(seqNum);
            if (weakRef == null) {
                return;
            }
            res = weakRef.get();
            refIdx.remove(res.ref);
        }
        res.set(message);
    }

    // Unit testing.
    final int size() {
        synchronized (this) {
            return Math.max(refIdx.size(), seqNumIdx.size());
        }
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import com.swirlycloud.twirly.exception.DuplicateException;

import quickfix.Message;
import quickfix.SessionID;

public class FixCacheTest {

    private static final @NonNull SessionID SESSION_ID = new SessionID("FIX.4.4", "Foo", "Bar");

    @Test
    public final void testWeakRef() throws DuplicateException {
        final List<String> retain = new ArrayList<>();
        final FixCache cache = new FixCache(SESSION_ID);
        for (int i = 1; i < 1000000; ++i) {
            final String ref = String.valueOf(i);
            assert ref != null;
            // Retain key.
            retain.add(ref);
            cache.putRequest(ref, i);
            if (cache.size() < i) {
                return;
            }
            Thread.yield();
        }
        assertTrue(false);
    }

    @Test
    public final void testGetAndSetRef()
            throws DuplicateException, ExecutionException, InterruptedException {
        final FixCache cache = new FixCache(SESSION_ID);
        final Future<Message> fut = cache.putRequest("foo", 1);
        assertFalse(fut.isDone());
        final Message message = new Message();
        cache.setResponse("foo", message);
        assertTrue(fut.isDone());
        assertEquals(message, fut.get());
        // Should not throw.
        cache.putRequest("foo", 1);
    }

    @Test
    public final void testGetAndSetSeqNum()
            throws DuplicateException, ExecutionException, InterruptedException {
        final FixCache cache = new FixCache(SESSION_ID);
        final Future<Message> fut = cache.putRequest("foo", 1);
        assertFalse(fut.isDone());
        final Message message = new Message();
        cache.setResponse(1, message);
        assertTrue(fut.isDone());
        assertEquals(message, fut.get());
        // Should not throw.
        cache.putRequest("foo", 1);
    }

    @Test(expected = DuplicateException.class)
    public final void testDupRef() throws DuplicateException {
        final FixCache cache = new FixCache(SESSION_ID);
        @SuppressWarnings("unused")
        final Future<Message> fut = cache.putRequest("foo", 1);
        cache.putRequest("foo", 2);
    }

    @Test(expected = DuplicateException.class)
    public final void testSeqNum() throws DuplicateException {
        final FixCache cache = new FixCache(SESSION_ID);
        @SuppressWarnings("unused")
        final Future<Message> fut = cache.putRequest("foo", 1);
        cache.putRequest("bar", 1);
    }
}

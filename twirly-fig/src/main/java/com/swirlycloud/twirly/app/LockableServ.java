/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import com.swirlycloud.twirly.concurrent.AsyncModel;

/**
 * Serv with methods for acquiring read-write locks.
 * 
 * @author Mark Aylett
 */
public final class LockableServ extends Serv {

    private static final int PERMITS = Runtime.getRuntime().availableProcessors();
    private final Semaphore sem = new Semaphore(PERMITS);

    public LockableServ(AsyncModel model) throws InterruptedException, ExecutionException {
        super(model);
    }

    public LockableServ(Model model) {
        super(model);
    }

    public final void acquireRead() {
        sem.acquireUninterruptibly(1);
    }

    public final void releaseRead() {
        sem.release(1);
    }

    public final void acquireWrite() {
        sem.acquireUninterruptibly(PERMITS);
    }

    public final void releaseWrite() {
        sem.release(PERMITS);
    }
}

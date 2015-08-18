/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import java.util.concurrent.Semaphore;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.Journ;
import com.swirlycloud.twirly.io.Model;

/**
 * Serv with methods for acquiring read-write locks.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class LockableServ extends Serv {

    private static final int PERMITS = Runtime.getRuntime().availableProcessors();
    private final Semaphore sem = new Semaphore(PERMITS);

    public LockableServ(Model model, Journ journ, Factory factory, long now)
            throws InterruptedException {
        super(model, journ, factory, now);
    }

    public LockableServ(Datastore datastore, Factory factory, long now) throws InterruptedException {
        super(datastore, factory, now);
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

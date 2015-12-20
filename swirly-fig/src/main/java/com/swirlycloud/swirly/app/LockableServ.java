/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.app;

import java.util.concurrent.Semaphore;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.io.Cache;
import com.swirlycloud.swirly.io.Datastore;
import com.swirlycloud.swirly.io.Journ;
import com.swirlycloud.swirly.io.Model;

/**
 * Serv with methods for acquiring read-write locks.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class LockableServ extends Serv {

    private static final int PERMITS = Runtime.getRuntime().availableProcessors();
    private final Semaphore sem = new Semaphore(PERMITS);

    public LockableServ(Model model, Journ journ, Cache cache, long now)
            throws NotFoundException, ServiceUnavailableException, InterruptedException {
        super(model, journ, cache, now);
    }

    public LockableServ(Datastore datastore, Cache cache, long now)
            throws NotFoundException, ServiceUnavailableException, InterruptedException {
        super(datastore, cache, now);
    }

    public final int readLock() {
        sem.acquireUninterruptibly(1);
        return 1;
    }

    public final int writeLock() {
        sem.acquireUninterruptibly(PERMITS);
        return PERMITS;
    }

    public final int demoteLock() {
        final int permits = PERMITS - 1;
        sem.release(permits);
        return permits;
    }

    public final void unlock(int permits) {
        sem.release(permits);
    }
}

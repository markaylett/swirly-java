/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.UncheckedExecutionException;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.node.SlNode;

public final class AsyncDatastore implements Datastore {

    /**
     * Bounded queue capacity.
     */
    private static int CAPACITY = 1000;
    private static final Logger log = Logger.getLogger(AsyncDatastore.class.getName());
    private final Datastore datastore;
    private final ExecutorService service;

    private final @Nullable <T> T get(Future<T> future) throws InterruptedException {
        try {
            return future.get();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    public AsyncDatastore(Datastore datastore) {
        this.datastore = datastore;
        service = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(CAPACITY));
    }

    @Override
    public final void close() throws Exception {
        service.shutdown();
        if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
            service.shutdownNow();
        }
        datastore.close();
    }

    @Override
    public final @Nullable MnemRbTree selectAsset() throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectAsset();
            }
        }));
    }

    @Override
    public final @Nullable MnemRbTree selectContr() throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectContr();
            }
        }));
    }

    @Override
    public final @Nullable MnemRbTree selectMarket() throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectMarket();
            }
        }));
    }

    @Override
    public final @Nullable MnemRbTree selectTrader() throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectTrader();
            }
        }));
    }

    @Override
    public final @Nullable String selectTraderByEmail(@NonNull final String email) throws InterruptedException {
        return get(service.submit(new Callable<String>() {
            @Override
            public final String call() throws Exception {
                return datastore.selectTraderByEmail(email);
            }
        }));
    }

    @Override
    public final @Nullable SlNode selectOrder() throws InterruptedException {
        return get(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.selectOrder();
            }
        }));
    }

    @Override
    public final @Nullable SlNode selectTrade() throws InterruptedException {
        return get(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.selectTrade();
            }
        }));
    }

    @Override
    public final @Nullable SlNode selectPosn(final int busDay) throws InterruptedException {
        return get(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.selectPosn(busDay);
            }
        }));
    }

    @Override
    public final void insertMarket(final String mnem, final String display, final String contr,
            final int settlDay, final int expiryDay, final int state) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.insertMarket(mnem, display, contr, settlDay, expiryDay, state);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to insert market", t);
                }
            }
        });
    }

    @Override
    public final void updateMarket(final String mnem, final String display, final int state) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.updateMarket(mnem, display, state);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to update market", t);
                }
            }
        });
    }

    @Override
    public final void insertTrader(final String mnem, final String display, final String email) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.insertTrader(mnem, display, email);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to insert trader", t);
                }
            }
        });
    }

    @Override
    public final void updateTrader(final String mnem, final String display)
            throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.updateTrader(mnem, display);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to update trader", t);
                }
            }
        });
    }

    @Override
    public final void insertExec(final Exec exec) throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.insertExec(exec);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to insert exec", t);
                }
            }
        });
    }

    @Override
    public final void insertExecList(final String market, final SlNode first)
            throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.insertExecList(market, first);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to insert exec-list", t);
                }
            }
        });
    }

    @Override
    public final void archiveOrder(final String market, final long id, final long modified)
            throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.archiveOrder(market, id, modified);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to archive order", t);
                }
            }
        });
    }

    @Override
    public final void archiveTrade(final String market, final long id, final long modified)
            throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.archiveTrade(market, id, modified);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to archive trade", t);
                }
            }
        });
    }
}

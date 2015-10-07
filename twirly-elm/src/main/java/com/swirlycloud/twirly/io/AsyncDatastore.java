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
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.UncheckedExecutionException;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.node.JslNode;
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
    public final @Nullable MnemRbTree selectAsset(@NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final @Nullable MnemRbTree call() throws Exception {
                return datastore.selectAsset(factory);
            }
        }));
    }

    @Override
    public final @Nullable MnemRbTree selectContr(@NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectContr(factory);
            }
        }));
    }

    @Override
    public final @Nullable MnemRbTree selectMarket(@NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectMarket(factory);
            }
        }));
    }

    @Override
    public final @Nullable MnemRbTree selectTrader(@NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectTrader(factory);
            }
        }));
    }

    @Override
    public final @Nullable String selectTraderByEmail(@NonNull final String email,
            @NonNull final Factory factory) throws InterruptedException {
        return get(service.submit(new Callable<String>() {
            @Override
            public final String call() throws Exception {
                return datastore.selectTraderByEmail(email, factory);
            }
        }));
    }

    @Override
    public final @Nullable MnemRbTree selectView(@NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<MnemRbTree>() {
            @Override
            public final MnemRbTree call() throws Exception {
                return datastore.selectView(factory);
            }
        }));
    }

    @Override
    public final @Nullable SlNode selectOrder(@NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.selectOrder(factory);
            }
        }));
    }

    @Override
    public final @Nullable InstructTree selectOrder(@NonNull final String trader,
            @NonNull final Factory factory) throws InterruptedException {
        return get(service.submit(new Callable<InstructTree>() {
            @Override
            public final InstructTree call() throws Exception {
                return datastore.selectOrder(trader, factory);
            }
        }));
    }

    @Override
    public final @Nullable SlNode selectTrade(@NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.selectTrade(factory);
            }
        }));
    }

    @Override
    public final @Nullable InstructTree selectTrade(@NonNull final String trader,
            @NonNull final Factory factory) throws InterruptedException {
        return get(service.submit(new Callable<InstructTree>() {
            @Override
            public final InstructTree call() throws Exception {
                return datastore.selectTrade(trader, factory);
            }
        }));
    }

    @Override
    public final @Nullable SlNode selectPosn(final int busDay, @NonNull final Factory factory)
            throws InterruptedException {
        return get(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.selectPosn(busDay, factory);
            }
        }));
    }

    @Override
    public final @Nullable TraderPosnTree selectPosn(@NonNull final String trader, final int busDay,
            @NonNull final Factory factory) throws InterruptedException {
        return get(service.submit(new Callable<TraderPosnTree>() {
            @Override
            public final TraderPosnTree call() throws Exception {
                return datastore.selectPosn(trader, busDay, factory);
            }
        }));
    }

    @Override
    public final void insertMarket(final @NonNull String mnem, final @Nullable String display,
            final @NonNull String contr, final int settlDay, final int expiryDay, final int state) {
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
    public final void updateMarket(final @NonNull String mnem, final @Nullable String display,
            final int state) {
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
    public final void insertTrader(final @NonNull String mnem, final @Nullable String display,
            final @NonNull String email) {
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
    public final void updateTrader(final @NonNull String mnem, final @Nullable String display)
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
    public final void insertExec(final @NonNull Exec exec) throws NotFoundException {
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
    public final void insertExecList(final @NonNull String market, final @NonNull JslNode first)
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
    public final void insertExecList(final @NonNull JslNode first) throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.insertExecList(first);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to insert exec-list", t);
                }
            }
        });
    }

    @Override
    public final void archiveOrder(final @NonNull String market, final long id, final long modified)
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
    public final void archiveOrderList(@NonNull final String market, @NonNull final JslNode first,
            final long modified) throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.archiveOrderList(market, first, modified);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to archive order-list", t);
                }
            }
        });
    }

    @Override
    public final void archiveOrderList(@NonNull final JslNode first, final long modified)
            throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.archiveOrderList(first, modified);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to archive order-list", t);
                }
            }
        });
    }

    @Override
    public final void archiveTrade(final @NonNull String market, final long id, final long modified)
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

    @Override
    public final void archiveTradeList(@NonNull final String market, @NonNull final JslNode first,
            final long modified) throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.archiveTradeList(market, first, modified);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to archive trade-list", t);
                }
            }
        });
    }

    @Override
    public final void archiveTradeList(@NonNull final JslNode first, final long modified)
            throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.archiveTradeList(first, modified);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to archive trade-list", t);
                }
            }
        });
    }
}

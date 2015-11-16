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
import com.swirlycloud.twirly.domain.Quote;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.UncheckedExecutionException;
import com.swirlycloud.twirly.intrusive.MarketViewTree;
import com.swirlycloud.twirly.intrusive.RecTree;
import com.swirlycloud.twirly.intrusive.RequestIdTree;
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

    private final <T> T getNullable(Future<T> future) throws InterruptedException {
        try {
            return future.get();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    private final @NonNull <T> T getNonNull(Future<T> future) throws InterruptedException {
        try {
            final T value = future.get();
            assert value != null;
            return value;
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
    public final @NonNull RecTree readAsset(@NonNull final Factory factory)
            throws InterruptedException {
        return getNonNull(service.submit(new Callable<RecTree>() {
            @Override
            public final @NonNull RecTree call() throws Exception {
                return datastore.readAsset(factory);
            }
        }));
    }

    @Override
    public final @NonNull RecTree readContr(@NonNull final Factory factory)
            throws InterruptedException {
        return getNonNull(service.submit(new Callable<RecTree>() {
            @Override
            public final RecTree call() throws Exception {
                return datastore.readContr(factory);
            }
        }));
    }

    @Override
    public final @NonNull RecTree readMarket(@NonNull final Factory factory)
            throws InterruptedException {
        return getNonNull(service.submit(new Callable<RecTree>() {
            @Override
            public final RecTree call() throws Exception {
                return datastore.readMarket(factory);
            }
        }));
    }

    @Override
    public final @NonNull RecTree readTrader(@NonNull final Factory factory)
            throws InterruptedException {
        return getNonNull(service.submit(new Callable<RecTree>() {
            @Override
            public final RecTree call() throws Exception {
                return datastore.readTrader(factory);
            }
        }));
    }

    @Override
    public final @Nullable String readTraderByEmail(@NonNull final String email,
            @NonNull final Factory factory) throws InterruptedException {
        return getNullable(service.submit(new Callable<String>() {
            @Override
            public final String call() throws Exception {
                return datastore.readTraderByEmail(email, factory);
            }
        }));
    }

    @Override
    public final @NonNull MarketViewTree readView(@NonNull final Factory factory)
            throws InterruptedException {
        return getNonNull(service.submit(new Callable<MarketViewTree>() {
            @Override
            public final MarketViewTree call() throws Exception {
                return datastore.readView(factory);
            }
        }));
    }

    @Override
    public final @Nullable SlNode readOrder(@NonNull final Factory factory)
            throws InterruptedException {
        return getNullable(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.readOrder(factory);
            }
        }));
    }

    @Override
    public final @NonNull RequestIdTree readOrder(@NonNull final String trader,
            @NonNull final Factory factory) throws InterruptedException {
        return getNonNull(service.submit(new Callable<RequestIdTree>() {
            @Override
            public final RequestIdTree call() throws Exception {
                return datastore.readOrder(trader, factory);
            }
        }));
    }

    @Override
    public final @Nullable SlNode readTrade(@NonNull final Factory factory)
            throws InterruptedException {
        return getNullable(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.readTrade(factory);
            }
        }));
    }

    @Override
    public final @NonNull RequestIdTree readTrade(@NonNull final String trader,
            @NonNull final Factory factory) throws InterruptedException {
        return getNonNull(service.submit(new Callable<RequestIdTree>() {
            @Override
            public final RequestIdTree call() throws Exception {
                return datastore.readTrade(trader, factory);
            }
        }));
    }

    @Override
    public final @Nullable SlNode readPosn(final int busDay, @NonNull final Factory factory)
            throws InterruptedException {
        return getNullable(service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return datastore.readPosn(busDay, factory);
            }
        }));
    }

    @Override
    public final @NonNull TraderPosnTree readPosn(@NonNull final String trader, final int busDay,
            @NonNull final Factory factory) throws InterruptedException {
        return getNonNull(service.submit(new Callable<TraderPosnTree>() {
            @Override
            public final TraderPosnTree call() throws Exception {
                return datastore.readPosn(trader, busDay, factory);
            }
        }));
    }

    @Override
    public final void createMarket(final @NonNull String mnem, final @Nullable String display,
            final @NonNull String contr, final int settlDay, final int expiryDay, final int state) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.createMarket(mnem, display, contr, settlDay, expiryDay, state);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to create market", t);
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
    public final void createTrader(final @NonNull String mnem, final @Nullable String display,
            final @NonNull String email) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.createTrader(mnem, display, email);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to create trader", t);
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
    public final void createExec(final @NonNull Exec exec) throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.createExec(exec);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to create exec", t);
                }
            }
        });
    }

    @Override
    public final void createExecList(final @NonNull String market, final @NonNull JslNode first)
            throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.createExecList(market, first);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to create exec-list", t);
                }
            }
        });
    }

    @Override
    public final void createExecList(final @NonNull JslNode first) throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.createExecList(first);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to create exec-list", t);
                }
            }
        });
    }

    @Override
    public final void createQuote(@NonNull final Quote quote) throws NotFoundException {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    datastore.createQuote(quote);
                } catch (final Throwable t) {
                    log.log(Level.SEVERE, "failed to create quote", t);
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

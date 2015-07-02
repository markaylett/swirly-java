/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.SlNode;

public final class AsyncModelService implements AsyncModel {
    /**
     * Bounded queue capacity.
     */
    private static int CAPACITY = 1000;
    private static final Logger log = Logger.getLogger(AsyncModelService.class.getName());
    private final Model model;
    private final ExecutorService service;

    public AsyncModelService(Model model) {
        this.model = model;
        service = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(CAPACITY));
    }

    @Override
    public final void close() throws Exception {
        service.shutdown();
        if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
            service.shutdownNow();
        }
        model.close();
    }

    @Override
    public final void insertMarket(final String mnem, final String display, final String contr,
            final int settlDay, final int expiryDay, final int state) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    model.insertMarket(mnem, display, contr, settlDay, expiryDay, state);
                } catch (Throwable t) {
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
                    model.updateMarket(mnem, display, state);
                } catch (Throwable t) {
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
                    model.insertTrader(mnem, display, email);
                } catch (Throwable t) {
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
                    model.updateTrader(mnem, display);
                } catch (Throwable t) {
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
                    model.insertExec(exec);
                } catch (Throwable t) {
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
                    model.insertExecList(market, first);
                } catch (Throwable t) {
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
                    model.archiveOrder(market, id, modified);
                } catch (Throwable t) {
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
                    model.archiveTrade(market, id, modified);
                } catch (Throwable t) {
                    log.log(Level.SEVERE, "failed to archive trade", t);
                }
            }
        });
    }

    @SuppressWarnings("null")
    @Override
    public final @NonNull Future<SlNode> selectAsset() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectAsset();
            }
        });
    }

    @SuppressWarnings("null")
    @Override
    public final @NonNull Future<SlNode> selectContr() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectContr();
            }
        });
    }

    @SuppressWarnings("null")
    @Override
    public final @NonNull Future<SlNode> selectMarket() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectMarket();
            }
        });
    }

    @SuppressWarnings("null")
    @Override
    public final @NonNull Future<SlNode> selectTrader() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectTrader();
            }
        });
    }

    @SuppressWarnings("null")
    @Override
    public final @NonNull Future<SlNode> selectOrder() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectOrder();
            }
        });
    }

    @SuppressWarnings("null")
    @Override
    public final @NonNull Future<SlNode> selectTrade() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectTrade();
            }
        });
    }

    @SuppressWarnings("null")
    @Override
    public final @NonNull Future<SlNode> selectPosn() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectPosn();
            }
        });
    }
}

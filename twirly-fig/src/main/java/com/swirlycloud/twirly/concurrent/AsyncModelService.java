/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.swirlycloud.twirly.app.Model;
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
            final int settlDay, final int expiryDay) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                model.insertMarket(mnem, display, contr, settlDay, expiryDay);
            }
        });
    }

    @Override
    public final void insertTrader(final String mnem, final String display, final String email) {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                model.insertTrader(mnem, display, email);
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
                } catch (NotFoundException e) {
                    log.log(Level.SEVERE, "failed to insert exec", e);
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
                } catch (NotFoundException e) {
                    log.log(Level.SEVERE, "failed to insert exec-list", e);
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
                } catch (NotFoundException e) {
                    log.log(Level.SEVERE, "failed to archive order", e);
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
                } catch (NotFoundException e) {
                    log.log(Level.SEVERE, "failed to archive trade", e);
                }
            }
        });
    }

    @Override
    public final Future<SlNode> selectAsset() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectAsset();
            }
        });
    }

    @Override
    public final Future<SlNode> selectContr() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectContr();
            }
        });
    }

    @Override
    public final Future<SlNode> selectMarket() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectMarket();
            }
        });
    }

    @Override
    public final Future<SlNode> selectTrader() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectTrader();
            }
        });
    }

    @Override
    public final Future<SlNode> selectOrder() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectOrder();
            }
        });
    }

    @Override
    public final Future<SlNode> selectTrade() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectTrade();
            }
        });
    }

    @Override
    public final Future<SlNode> selectPosn() {
        return service.submit(new Callable<SlNode>() {
            @Override
            public final SlNode call() throws Exception {
                return model.selectPosn();
            }
        });
    }
}

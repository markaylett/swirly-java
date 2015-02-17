package com.swirlycloud.twirly.concurrent;
public final class AsyncModelService implements AsyncModel {
    private static final Logger log = Logger.getLogger(AsyncModelService.class.getName());
    private final Model model;
    private final ExecutorService service = Executors.newFixedThreadPool(1);

    public AsyncModelService(Model model) {
        this.model = model;
    }

    @Override
    public final void close() throws Exception {
        service.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    model.close();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "failed to close model", e);
                    e.printStackTrace();
                }
            }
        });
        service.shutdown();
        if (!service.awaitTermination(10, TimeUnit.SECONDS))
            service.shutdownNow();
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

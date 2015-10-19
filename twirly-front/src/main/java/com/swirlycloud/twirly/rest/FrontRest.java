/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.rest.RestUtil.getViewsParam;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.OrderNotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.exception.UncheckedExecutionException;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.rec.Rec;
import com.swirlycloud.twirly.rec.RecType;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class FrontRest implements Rest {

    private final Model model;
    private final Cache cache;
    private final Factory factory;

    private final MnemRbTree selectAsset() throws ExecutionException, InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("asset").get();
        if (tree == null) {
            tree = model.selectAsset(factory);
            cache.insert("asset", tree);
        }
        return tree;
    }

    private final MnemRbTree selectContr() throws ExecutionException, InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("contr").get();
        if (tree == null) {
            tree = model.selectContr(factory);
            cache.insert("contr", tree);
        }
        return tree;
    }

    private final MnemRbTree selectMarket() throws ExecutionException, InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("market").get();
        if (tree == null) {
            tree = model.selectMarket(factory);
            cache.insert("market", tree);
        }
        return tree;
    }

    private final MnemRbTree selectTrader() throws ExecutionException, InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("trader").get();
        if (tree == null) {
            tree = model.selectTrader(factory);
            cache.insert("trader", tree);
        }
        return tree;
    }

    private final MnemRbTree selectRec(RecType recType)
            throws ExecutionException, InterruptedException {
        MnemRbTree tree = null;
        switch (recType) {
        case ASSET:
            tree = selectAsset();
            break;
        case CONTR:
            tree = selectContr();
            break;
        case MARKET:
            tree = selectMarket();
            break;
        case TRADER:
            tree = selectTrader();
            break;
        }
        assert tree != null;
        return tree;
    }

    private final @Nullable String selectTraderByEmail(String email)
            throws ExecutionException, InterruptedException {
        final String key = "trader:" + email;
        String trader = (String) cache.select(key).get();
        if (trader == null) {
            trader = model.selectTraderByEmail(email, factory);
            // An empty value indicates that there is no trader with this email, as opposed to a
            // null value, which indicates that the cache is empty.
            cache.insert(key, trader != null ? trader : "");
        }
        return trader != null && !trader.isEmpty() ? trader : null;
    }

    private final MnemRbTree selectView() throws ExecutionException, InterruptedException {
        MnemRbTree tree = (MnemRbTree) cache.select("view").get();
        if (tree == null) {
            tree = model.selectView(factory);
            cache.insert("view", tree);
        }
        return tree;
    }

    private final InstructTree selectOrder(String trader)
            throws ExecutionException, InterruptedException {
        final String key = "order:" + trader;
        InstructTree tree = (InstructTree) cache.select(key).get();
        if (tree == null) {
            tree = model.selectOrder(trader, factory);
            cache.insert(key, tree);
        }
        return tree;
    }

    private final InstructTree selectTrade(String trader)
            throws ExecutionException, InterruptedException {
        final String key = "trade:" + trader;
        InstructTree tree = (InstructTree) cache.select(key).get();
        if (tree == null) {
            tree = model.selectTrade(trader, factory);
            cache.insert(key, tree);
        }
        return tree;
    }

    private final TraderPosnTree selectPosn(String trader, int busDay)
            throws ExecutionException, InterruptedException {
        final String key = "posn:" + trader;
        TraderPosnTree tree = (TraderPosnTree) cache.select(key).get();
        if (tree == null) {
            tree = model.selectPosn(trader, busDay, factory);
            cache.insert(key, tree);
        }
        return tree;
    }

    private final long selectTimeout() {
        return 0;
    }

    public FrontRest(Model model, Cache cache, Factory factory) {
        this.model = model;
        this.cache = cache;
        this.factory = factory;
    }

    @Override
    public final @Nullable String findTraderByEmail(String email)
            throws ServiceUnavailableException {
        try {
            return selectTraderByEmail(email);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getRec(boolean withTraders, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            out.append("{\"assets\":");
            toJsonArray(selectAsset().getFirst(), params, out);
            out.append(",\"contrs\":");
            toJsonArray(selectContr().getFirst(), params, out);
            out.append(",\"markets\":");
            toJsonArray(selectMarket().getFirst(), params, out);
            if (withTraders) {
                out.append(",\"traders\":");
                toJsonArray(selectTrader().getFirst(), params, out);
            }
            out.append('}');
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getRec(RecType recType, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            final MnemRbTree tree = selectRec(recType);
            toJsonArray(tree.getFirst(), params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        try {
            final MnemRbTree tree = selectRec(recType);
            final Rec rec = (Rec) tree.find(mnem);
            if (rec == null) {
                throw new NotFoundException(String.format("record '%s' does not exist", mnem));
            }
            rec.toJson(params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getView(Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            toJsonArray(selectView().getFirst(), params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getView(String market, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            RestUtil.getView(selectView().getFirst(), market, params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getSess(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            out.append("{\"orders\":");
            toJsonArray(selectOrder(trader).getFirst(), params, out);
            out.append(",\"trades\":");
            toJsonArray(selectTrade(trader).getFirst(), params, out);
            out.append(",\"posns\":");
            final int busDay = getBusDate(now).toJd();
            toJsonArray(selectPosn(trader, busDay).getFirst(), params, out);
            if (getViewsParam(params)) {
                out.append(",\"views\":");
                toJsonArray(selectView().getFirst(), params, out);
            }
            out.append('}');
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getOrder(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            toJsonArray(selectOrder(trader).getFirst(), params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getOrder(String trader, String market, Params params, long now,
            Appendable out) throws ServiceUnavailableException, IOException {
        try {
            RestUtil.getOrder(selectOrder(trader).getFirst(), market, params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getOrder(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        try {
            final InstructTree tree = selectOrder(trader);
            final Order order = (Order) tree.find(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            order.toJson(params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getTrade(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            toJsonArray(selectTrade(trader).getFirst(), params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getTrade(String trader, String market, Params params, long now,
            Appendable out) throws ServiceUnavailableException, IOException {
        try {
            RestUtil.getTrade(selectTrade(trader).getFirst(), market, params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getTrade(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        try {
            final InstructTree tree = selectTrade(trader);
            final Exec trade = (Exec) tree.find(market, id);
            if (trade == null) {
                throw new NotFoundException(String.format("trade '%d' does not exist", id));
            }
            trade.toJson(params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getPosn(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            final int busDay = getBusDate(now).toJd();
            toJsonArray(selectPosn(trader, busDay).getFirst(), params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getPosn(String trader, String contr, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        try {
            RestUtil.getPosn(selectTrade(trader).getFirst(), contr, params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getPosn(String trader, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        try {
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree tree = selectPosn(trader, busDay);
            final Posn posn = (Posn) tree.find(contr, maybeIsoToJd(settlDate));
            if (posn == null) {
                throw new NotFoundException(
                        String.format("posn for '%s' on '%d' does not exist", contr, settlDate));
            }
            posn.toJson(params, out);
            return selectTimeout();
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }
}

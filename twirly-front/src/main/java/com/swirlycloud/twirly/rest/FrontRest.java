/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.rest.RestUtil.getViewsParam;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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
    private volatile long timeout;

    private static String toKey(RecType recType) {
        String name;
        switch (recType) {
        case ASSET:
            name = "asset";
            break;
        case CONTR:
            name = "contr";
            break;
        case MARKET:
            name = "market";
            break;
        case TRADER:
            name = "trader";
            break;
        default:
            throw new IllegalArgumentException("invalid rec-type");
        }
        assert name != null;
        return name;
    }

    private final MnemRbTree readAsset(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.readAsset(factory);
            cache.create("asset", tree);
        }
        return tree;
    }

    private final MnemRbTree readContr(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.readContr(factory);
            cache.create("contr", tree);
        }
        return tree;
    }

    private final MnemRbTree readMarket(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.readMarket(factory);
            cache.create("market", tree);
        }
        return tree;
    }

    private final MnemRbTree readTrader(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.readTrader(factory);
            cache.create("trader", tree);
        }
        return tree;
    }

    private final MnemRbTree readRec(RecType recType, @Nullable Object value)
            throws InterruptedException {
        MnemRbTree tree = null;
        switch (recType) {
        case ASSET:
            tree = readAsset(value);
            break;
        case CONTR:
            tree = readContr(value);
            break;
        case MARKET:
            tree = readMarket(value);
            break;
        case TRADER:
            tree = readTrader(value);
            break;
        }
        assert tree != null;
        return tree;
    }

    private final MnemRbTree readView(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.readView(factory);
            cache.create("view", tree);
        }
        return tree;
    }

    private final InstructTree readOrder(String trader, @Nullable Object value)
            throws InterruptedException {
        InstructTree tree = (InstructTree) value;
        if (tree == null) {
            tree = model.readOrder(trader, factory);
            cache.create("order:" + trader, tree);
        }
        return tree;
    }

    private final InstructTree readTrade(String trader, @Nullable Object value)
            throws InterruptedException {
        InstructTree tree = (InstructTree) value;
        if (tree == null) {
            tree = model.readTrade(trader, factory);
            cache.create("trade:" + trader, tree);
        }
        return tree;
    }

    private final TraderPosnTree readPosn(String trader, int busDay, @Nullable Object value)
            throws InterruptedException {
        TraderPosnTree tree = (TraderPosnTree) value;
        if (tree == null) {
            tree = model.readPosn(trader, busDay, factory);
            cache.create("posn:" + trader, tree);
        }
        return tree;
    }

    private final long readTimeout(@Nullable Object value) {
        return value != null ? (Long) value : 0;
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
            final String key = "trader:" + email;
            String trader = (String) cache.read(key).get();
            if (trader == null) {
                trader = model.readTraderByEmail(email, factory);
                // An empty value indicates that there is no trader with this email, as opposed to a
                // null value, which indicates that the cache is empty.
                cache.create(key, trader != null ? trader : "");
            }
            return trader != null && !trader.isEmpty() ? trader : null;
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getRec(boolean withTraders, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final Collection<String> keys = Arrays.asList("asset", "contr", "market", "trader",
                "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final MnemRbTree assets = readAsset(map.get("asset"));
            final MnemRbTree contrs = readAsset(map.get("contr"));
            final MnemRbTree markets = readAsset(map.get("market"));
            timeout = readTimeout(map.get("timeout"));

            out.append("{\"assets\":");
            toJsonArray(assets.getFirst(), params, out);
            out.append(",\"contrs\":");
            toJsonArray(contrs.getFirst(), params, out);
            out.append(",\"markets\":");
            toJsonArray(markets.getFirst(), params, out);
            if (withTraders) {
                final MnemRbTree traders = readAsset(map.get("trader"));
                out.append(",\"traders\":");
                toJsonArray(traders.getFirst(), params, out);
            }
            out.append('}');
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getRec(RecType recType, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final String name = toKey(recType);
        final Collection<String> keys = Arrays.asList(name, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final MnemRbTree recs = readRec(recType, map.get(name));
            timeout = readTimeout(map.get("timeout"));

            toJsonArray(recs.getFirst(), params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {

        final String name = toKey(recType);
        final Collection<String> keys = Arrays.asList(name, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final MnemRbTree recs = readRec(recType, map.get(name));
            timeout = readTimeout(map.get("timeout"));

            final Rec rec = (Rec) recs.find(mnem);
            if (rec == null) {
                throw new NotFoundException(String.format("record '%s' does not exist", mnem));
            }
            rec.toJson(params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getView(Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final Collection<String> keys = Arrays.asList("view", "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final MnemRbTree views = readView(map.get("view"));
            timeout = readTimeout(map.get("timeout"));

            toJsonArray(views.getFirst(), params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getView(String market, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final Collection<String> keys = Arrays.asList("view", "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final MnemRbTree views = readView(map.get("view"));
            timeout = readTimeout(map.get("timeout"));

            RestUtil.getView(views.getFirst(), market, params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getSess(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final String orderKey = "order:" + trader;
        final String tradeKey = "trade:" + trader;
        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, tradeKey, posnKey, "view",
                "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final InstructTree orders = readOrder(trader, map.get(orderKey));
            final InstructTree trades = readTrade(trader, map.get(tradeKey));
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));
            timeout = readTimeout(map.get("timeout"));

            out.append("{\"orders\":");
            toJsonArray(orders.getFirst(), params, out);
            out.append(",\"trades\":");
            toJsonArray(trades.getFirst(), params, out);
            out.append(",\"posns\":");
            toJsonArray(posns.getFirst(), params, out);
            if (getViewsParam(params)) {
                final MnemRbTree views = readView(map.get("view"));
                out.append(",\"views\":");
                toJsonArray(views.getFirst(), params, out);
            }
            out.append('}');
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getOrder(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final String orderKey = "order:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final InstructTree orders = readOrder(trader, map.get(orderKey));
            timeout = readTimeout(map.get("timeout"));

            toJsonArray(orders.getFirst(), params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getOrder(String trader, String market, Params params, long now,
            Appendable out) throws ServiceUnavailableException, IOException {

        final String orderKey = "order:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final InstructTree orders = readOrder(trader, map.get(orderKey));
            timeout = readTimeout(map.get("timeout"));

            RestUtil.getOrder(orders.getFirst(), market, params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getOrder(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {

        final String orderKey = "order:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final InstructTree orders = readOrder(trader, map.get(orderKey));
            timeout = readTimeout(map.get("timeout"));

            final Order order = (Order) orders.find(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            order.toJson(params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getTrade(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final String tradeKey = "trade:" + trader;
        final Collection<String> keys = Arrays.asList(tradeKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final InstructTree trades = readTrade(trader, map.get(tradeKey));
            timeout = readTimeout(map.get("timeout"));

            toJsonArray(trades.getFirst(), params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getTrade(String trader, String market, Params params, long now,
            Appendable out) throws ServiceUnavailableException, IOException {

        final String tradeKey = "trade:" + trader;
        final Collection<String> keys = Arrays.asList(tradeKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final InstructTree trades = readTrade(trader, map.get(tradeKey));
            timeout = readTimeout(map.get("timeout"));

            RestUtil.getTrade(trades.getFirst(), market, params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getTrade(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {

        final String tradeKey = "trade:" + trader;
        final Collection<String> keys = Arrays.asList(tradeKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final InstructTree trades = readTrade(trader, map.get(tradeKey));
            timeout = readTimeout(map.get("timeout"));

            final Exec trade = (Exec) trades.find(market, id);
            if (trade == null) {
                throw new NotFoundException(String.format("trade '%d' does not exist", id));
            }
            trade.toJson(params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getPosn(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(posnKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));
            timeout = readTimeout(map.get("timeout"));

            toJsonArray(posns.getFirst(), params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getPosn(String trader, String contr, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(posnKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));
            timeout = readTimeout(map.get("timeout"));

            RestUtil.getPosn(posns.getFirst(), contr, params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getPosn(String trader, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {

        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(posnKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));
            timeout = readTimeout(map.get("timeout"));

            final Posn posn = (Posn) posns.find(contr, maybeIsoToJd(settlDate));
            if (posn == null) {
                throw new NotFoundException(
                        String.format("posn for '%s' on '%d' does not exist", contr, settlDate));
            }
            posn.toJson(params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final long getTimeout() {
        return timeout;
    }
}

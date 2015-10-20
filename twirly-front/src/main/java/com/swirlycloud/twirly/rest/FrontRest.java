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

    private final MnemRbTree getAsset(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.selectAsset(factory);
            cache.insert("asset", tree);
        }
        return tree;
    }

    private final MnemRbTree getContr(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.selectContr(factory);
            cache.insert("contr", tree);
        }
        return tree;
    }

    private final MnemRbTree getMarket(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.selectMarket(factory);
            cache.insert("market", tree);
        }
        return tree;
    }

    private final MnemRbTree getTrader(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.selectTrader(factory);
            cache.insert("trader", tree);
        }
        return tree;
    }

    private final MnemRbTree getRec(RecType recType, @Nullable Object value)
            throws InterruptedException {
        MnemRbTree tree = null;
        switch (recType) {
        case ASSET:
            tree = getAsset(value);
            break;
        case CONTR:
            tree = getContr(value);
            break;
        case MARKET:
            tree = getMarket(value);
            break;
        case TRADER:
            tree = getTrader(value);
            break;
        }
        assert tree != null;
        return tree;
    }

    private final MnemRbTree getView(@Nullable Object value) throws InterruptedException {
        MnemRbTree tree = (MnemRbTree) value;
        if (tree == null) {
            tree = model.selectView(factory);
            cache.insert("view", tree);
        }
        return tree;
    }

    private final InstructTree getOrder(String trader, @Nullable Object value)
            throws InterruptedException {
        InstructTree tree = (InstructTree) value;
        if (tree == null) {
            tree = model.selectOrder(trader, factory);
            cache.insert("order:" + trader, tree);
        }
        return tree;
    }

    private final InstructTree getTrade(String trader, @Nullable Object value)
            throws InterruptedException {
        InstructTree tree = (InstructTree) value;
        if (tree == null) {
            tree = model.selectTrade(trader, factory);
            cache.insert("trade:" + trader, tree);
        }
        return tree;
    }

    private final TraderPosnTree getPosn(String trader, int busDay, @Nullable Object value)
            throws InterruptedException {
        TraderPosnTree tree = (TraderPosnTree) value;
        if (tree == null) {
            tree = model.selectPosn(trader, busDay, factory);
            cache.insert("posn:" + trader, tree);
        }
        return tree;
    }

    private final long getTimeout(@Nullable Object value) {
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
            String trader = (String) cache.select(key).get();
            if (trader == null) {
                trader = model.selectTraderByEmail(email, factory);
                // An empty value indicates that there is no trader with this email, as opposed to a
                // null value, which indicates that the cache is empty.
                cache.insert(key, trader != null ? trader : "");
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
    public final long getRec(boolean withTraders, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final Collection<String> keys = Arrays.asList("asset", "contr", "market", "trader",
                "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final MnemRbTree asset = getAsset(map.get("asset"));
            final MnemRbTree contr = getAsset(map.get("contr"));
            final MnemRbTree market = getAsset(map.get("market"));
            final long timeout = getTimeout(map.get("timeout"));

            out.append("{\"assets\":");
            toJsonArray(asset.getFirst(), params, out);
            out.append(",\"contrs\":");
            toJsonArray(contr.getFirst(), params, out);
            out.append(",\"markets\":");
            toJsonArray(market.getFirst(), params, out);
            if (withTraders) {
                final MnemRbTree trader = getAsset(map.get("trader"));
                out.append(",\"traders\":");
                toJsonArray(trader.getFirst(), params, out);
            }
            out.append('}');
            return timeout;
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

        final String name = toKey(recType);
        final Collection<String> keys = Arrays.asList(name, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final MnemRbTree tree = getRec(recType, map.get(name));
            final long timeout = getTimeout(map.get("timeout"));

            toJsonArray(tree.getFirst(), params, out);
            return timeout;
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

        final String name = toKey(recType);
        final Collection<String> keys = Arrays.asList(name, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final MnemRbTree tree = getRec(recType, map.get(name));
            final long timeout = getTimeout(map.get("timeout"));

            final Rec rec = (Rec) tree.find(mnem);
            if (rec == null) {
                throw new NotFoundException(String.format("record '%s' does not exist", mnem));
            }
            rec.toJson(params, out);
            return timeout;
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

        final Collection<String> keys = Arrays.asList("view", "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final MnemRbTree tree = getView(map.get("view"));
            final long timeout = getTimeout(map.get("timeout"));

            toJsonArray(tree.getFirst(), params, out);
            return timeout;
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

        final Collection<String> keys = Arrays.asList("view", "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final MnemRbTree tree = getView(map.get("view"));
            final long timeout = getTimeout(map.get("timeout"));

            RestUtil.getView(tree.getFirst(), market, params, out);
            return timeout;
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

        final String orderKey = "order:" + trader;
        final String tradeKey = "trade:" + trader;
        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, tradeKey, posnKey, "view",
                "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final InstructTree order = getOrder(trader, map.get(orderKey));
            final InstructTree trade = getTrade(trader, map.get(tradeKey));
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posn = getPosn(trader, busDay, map.get(posnKey));
            final long timeout = getTimeout(map.get("timeout"));

            out.append("{\"orders\":");
            toJsonArray(order.getFirst(), params, out);
            out.append(",\"trades\":");
            toJsonArray(trade.getFirst(), params, out);
            out.append(",\"posns\":");
            toJsonArray(posn.getFirst(), params, out);
            if (getViewsParam(params)) {
                final MnemRbTree view = getView(map.get("view"));
                out.append(",\"views\":");
                toJsonArray(view.getFirst(), params, out);
            }
            out.append('}');
            return timeout;
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

        final String orderKey = "order:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final InstructTree tree = getOrder(trader, map.get(orderKey));
            final long timeout = getTimeout(map.get("timeout"));

            toJsonArray(tree.getFirst(), params, out);
            return timeout;
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

        final String orderKey = "order:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final InstructTree tree = getOrder(trader, map.get(orderKey));
            final long timeout = getTimeout(map.get("timeout"));

            RestUtil.getOrder(tree.getFirst(), market, params, out);
            return timeout;
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

        final String orderKey = "order:" + trader;
        final Collection<String> keys = Arrays.asList(orderKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final InstructTree tree = getOrder(trader, map.get(orderKey));
            final long timeout = getTimeout(map.get("timeout"));

            final Order order = (Order) tree.find(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            order.toJson(params, out);
            return timeout;
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

        final String tradeKey = "trade:" + trader;
        final Collection<String> keys = Arrays.asList(tradeKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final InstructTree tree = getTrade(trader, map.get(tradeKey));
            final long timeout = getTimeout(map.get("timeout"));

            toJsonArray(tree.getFirst(), params, out);
            return timeout;
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

        final String tradeKey = "trade:" + trader;
        final Collection<String> keys = Arrays.asList(tradeKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final InstructTree tree = getTrade(trader, map.get(tradeKey));
            final long timeout = getTimeout(map.get("timeout"));

            RestUtil.getTrade(tree.getFirst(), market, params, out);
            return timeout;
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

        final String tradeKey = "trade:" + trader;
        final Collection<String> keys = Arrays.asList(tradeKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final InstructTree tree = getTrade(trader, map.get(tradeKey));
            final long timeout = getTimeout(map.get("timeout"));

            final Exec trade = (Exec) tree.find(market, id);
            if (trade == null) {
                throw new NotFoundException(String.format("trade '%d' does not exist", id));
            }
            trade.toJson(params, out);
            return timeout;
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

        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(posnKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree tree = getPosn(trader, busDay, map.get(posnKey));
            final long timeout = getTimeout(map.get("timeout"));

            toJsonArray(tree.getFirst(), params, out);
            return timeout;
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

        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(posnKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree tree = getPosn(trader, busDay, map.get(posnKey));
            final long timeout = getTimeout(map.get("timeout"));

            RestUtil.getPosn(tree.getFirst(), contr, params, out);
            return timeout;
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

        final String posnKey = "posn:" + trader;
        final Collection<String> keys = Arrays.asList(posnKey, "timeout");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.select(keys).get();
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree tree = getPosn(trader, busDay, map.get(posnKey));
            final long timeout = getTimeout(map.get("timeout"));

            final Posn posn = (Posn) tree.find(contr, maybeIsoToJd(settlDate));
            if (posn == null) {
                throw new NotFoundException(
                        String.format("posn for '%s' on '%d' does not exist", contr, settlDate));
            }
            posn.toJson(params, out);
            return timeout;
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }
}

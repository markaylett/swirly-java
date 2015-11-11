/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.rest.RestUtil.*;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.EntitySet;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.OrderNotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.exception.UncheckedExecutionException;
import com.swirlycloud.twirly.intrusive.MarketViewTree;
import com.swirlycloud.twirly.intrusive.RecTree;
import com.swirlycloud.twirly.intrusive.RequestIdTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.rec.Rec;
import com.swirlycloud.twirly.rec.RecType;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class FrontRest implements Rest {

    private static final RequestIdTree EMPTY_QUOTES = new RequestIdTree();

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

    private final RecTree readAsset(@Nullable Object value) throws InterruptedException {
        RecTree tree = (RecTree) value;
        if (tree == null) {
            tree = model.readAsset(factory);
            cache.create("asset", tree);
        }
        return tree;
    }

    private final RecTree readContr(@Nullable Object value) throws InterruptedException {
        RecTree tree = (RecTree) value;
        if (tree == null) {
            tree = model.readContr(factory);
            cache.create("contr", tree);
        }
        return tree;
    }

    private final RecTree readMarket(@Nullable Object value) throws InterruptedException {
        RecTree tree = (RecTree) value;
        if (tree == null) {
            tree = model.readMarket(factory);
            cache.create("market", tree);
        }
        return tree;
    }

    private final RecTree readTrader(@Nullable Object value) throws InterruptedException {
        RecTree tree = (RecTree) value;
        if (tree == null) {
            tree = model.readTrader(factory);
            cache.create("trader", tree);
        }
        return tree;
    }

    private final RecTree readRec(RecType recType, @Nullable Object value)
            throws InterruptedException {
        RecTree tree = null;
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

    private final MarketViewTree readView(@Nullable Object value) throws InterruptedException {
        MarketViewTree tree = (MarketViewTree) value;
        if (tree == null) {
            tree = model.readView(factory);
            cache.create("view", tree);
        }
        return tree;
    }

    private final RequestIdTree readOrder(String trader, @Nullable Object value)
            throws InterruptedException {
        RequestIdTree tree = (RequestIdTree) value;
        if (tree == null) {
            tree = model.readOrder(trader, factory);
            cache.create("order:" + trader, tree);
        }
        return tree;
    }

    private final RequestIdTree readTrade(String trader, @Nullable Object value)
            throws InterruptedException {
        RequestIdTree tree = (RequestIdTree) value;
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

    private final RequestIdTree readQuote(String trader, @Nullable Object value) {
        RequestIdTree tree = (RequestIdTree) value;
        if (tree == null) {
            tree = EMPTY_QUOTES;
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
    public final void getRec(EntitySet es, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {

        final Collection<String> keys = new ArrayList<>(5);
        keys.add("timeout");
        if (es.isAssetSet()) {
            keys.add("asset");
        }
        if (es.isContrSet()) {
            keys.add("contr");
        }
        if (es.isMarketSet()) {
            keys.add("market");
        }
        if (es.isTraderSet()) {
            keys.add("trader");
        }
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));

            int i = 0;
            out.append('{');
            if (es.isAssetSet()) {
                out.append("\"assets\":");
                final RecTree assets = readAsset(map.get("asset"));
                toJsonArray(assets.getFirst(), params, out);
                ++i;
            }
            if (es.isContrSet()) {
                if (i > 0) {
                    out.append(',');                    
                }
                out.append("\"contrs\":");
                final RecTree contrs = readContr(map.get("contr"));
                toJsonArray(contrs.getFirst(), params, out);
                ++i;
            }
            if (es.isMarketSet()) {
                if (i > 0) {
                    out.append(',');                    
                }
                out.append("\"markets\":");
                final RecTree markets = readMarket(map.get("market"));
                toJsonArray(markets.getFirst(), params, out);
                ++i;
            }
            if (es.isTraderSet()) {
                if (i > 0) {
                    out.append(',');                    
                }
                out.append("\"traders\":");
                final RecTree traders = readTrader(map.get("trader"));
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
        final Collection<String> keys = Arrays.asList("timeout", name);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RecTree recs = readRec(recType, map.get(name));

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
        final Collection<String> keys = Arrays.asList("timeout", name);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RecTree recs = readRec(recType, map.get(name));

            final Rec rec = recs.find(mnem);
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

        final Collection<String> keys = Arrays.asList("timeout", "view");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final MarketViewTree views = readView(map.get("view"));

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

        final Collection<String> keys = Arrays.asList("timeout", "view");
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final MarketViewTree views = readView(map.get("view"));

            RestUtil.filterMarket(views.getFirst(), market, params, out);
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

        final boolean withQuotes = getQuotesParam(params);
        final boolean withViews = getViewsParam(params);

        final String orderKey = "order:" + trader;
        final String tradeKey = "trade:" + trader;
        final String posnKey = "posn:" + trader;
        final String quoteKey = "quote:" + trader;

        final Collection<String> keys = new ArrayList<>(6);
        keys.add("timeout");
        keys.add(orderKey);
        keys.add(tradeKey);
        keys.add(posnKey);
        if (withQuotes) {
            keys.add(quoteKey);
        }
        if (withViews) {
            keys.add("view");
        }
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree orders = readOrder(trader, map.get(orderKey));
            final RequestIdTree trades = readTrade(trader, map.get(tradeKey));
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));

            out.append("{\"orders\":");
            toJsonArray(orders.getFirst(), params, out);
            out.append(",\"trades\":");
            toJsonArray(trades.getFirst(), params, out);
            out.append(",\"posns\":");
            toJsonArray(posns.getFirst(), params, out);
            if (withQuotes) {
                final RequestIdTree quotes = readQuote(trader, map.get(quoteKey));
                out.append(",\"quotes\":");
                toJsonArray(quotes.getFirst(), params, out);
            }
            if (withViews) {
                final MarketViewTree views = readView(map.get("view"));
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
        final Collection<String> keys = Arrays.asList("timeout", orderKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree orders = readOrder(trader, map.get(orderKey));

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
        final Collection<String> keys = Arrays.asList("timeout", orderKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree orders = readOrder(trader, map.get(orderKey));

            RestUtil.filterMarket(orders.getFirst(), market, params, out);
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
        final Collection<String> keys = Arrays.asList("timeout", orderKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree orders = readOrder(trader, map.get(orderKey));

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
        final Collection<String> keys = Arrays.asList("timeout", tradeKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree trades = readTrade(trader, map.get(tradeKey));

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
        final Collection<String> keys = Arrays.asList("timeout", tradeKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree trades = readTrade(trader, map.get(tradeKey));

            RestUtil.filterMarket(trades.getFirst(), market, params, out);
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
        final Collection<String> keys = Arrays.asList("timeout", tradeKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree trades = readTrade(trader, map.get(tradeKey));

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
        final Collection<String> keys = Arrays.asList("timeout", posnKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));

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
        final Collection<String> keys = Arrays.asList("timeout", posnKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));

            RestUtil.filterPosn(posns.getFirst(), contr, params, out);
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
        final Collection<String> keys = Arrays.asList("timeout", posnKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final int busDay = getBusDate(now).toJd();
            final TraderPosnTree posns = readPosn(trader, busDay, map.get(posnKey));

            final Posn posn = posns.find(contr, maybeIsoToJd(settlDate));
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
    public final void getQuote(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {

        final String quoteKey = "quote:" + trader;
        final Collection<String> keys = Arrays.asList("timeout", quoteKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree quotes = readQuote(trader, map.get(quoteKey));

            toJsonArray(quotes.getFirst(), params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getQuote(String trader, String market, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {

        final String quoteKey = "quote:" + trader;
        final Collection<String> keys = Arrays.asList("timeout", quoteKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree quotes = readQuote(trader, map.get(quoteKey));

            RestUtil.filterMarket(quotes.getFirst(), market, params, out);
        } catch (final ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getQuote(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {

        final String quoteKey = "quote:" + trader;
        final Collection<String> keys = Arrays.asList("timeout", quoteKey);
        assert keys != null;
        try {
            final Map<String, Object> map = cache.read(keys).get();
            timeout = readTimeout(map.get("timeout"));
            final RequestIdTree quotes = readQuote(trader, map.get(quoteKey));

            final Exec trade = (Exec) quotes.find(market, id);
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
    public final long getTimeout() {
        return timeout;
    }
}

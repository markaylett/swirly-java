/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.JulianDay.maybeJdToIso;
import static com.swirlycloud.twirly.util.JsonUtil.parseStartArray;
import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.app.LockableServ;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.entity.Asset;
import com.swirlycloud.twirly.entity.Contr;
import com.swirlycloud.twirly.entity.EntitySet;
import com.swirlycloud.twirly.entity.Exec;
import com.swirlycloud.twirly.entity.Factory;
import com.swirlycloud.twirly.entity.Market;
import com.swirlycloud.twirly.entity.MarketView;
import com.swirlycloud.twirly.entity.Order;
import com.swirlycloud.twirly.entity.Posn;
import com.swirlycloud.twirly.entity.Quote;
import com.swirlycloud.twirly.entity.Rec;
import com.swirlycloud.twirly.entity.RecType;
import com.swirlycloud.twirly.entity.Trader;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.Journ;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.JslNode;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("null")
public final @NonNullByDefault class BackUnrest {
    public static final class PosnKey {
        private final String contr;
        private final int settlDay;

        public PosnKey(String contr, int settlDay) {
            super();
            this.contr = contr;
            this.settlDay = settlDay;
        }

        @Override
        public final int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + contr.hashCode();
            result = prime * result + settlDay;
            return result;
        }

        @Override
        public final boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PosnKey other = (PosnKey) obj;
            if (!contr.equals(other.contr)) {
                return false;
            }
            if (settlDay != other.settlDay) {
                return false;
            }
            return true;
        }

        public final String getContr() {
            return contr;
        }

        public final int getSettlDay() {
            return settlDay;
        }
    }

    private final BackRest rest;

    private static void parseAssets(JsonParser p, Map<String, ? super Asset> out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Asset asset = Asset.parse(p);
                out.put(asset.getMnem(), asset);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parseContrs(JsonParser p, Map<String, ? super Contr> out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Contr contr = Contr.parse(p);
                out.put(contr.getMnem(), contr);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parseMarkets(JsonParser p, Map<String, ? super Market> out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Market market = Market.parse(p);
                out.put(market.getMnem(), market);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parseTraders(JsonParser p, Map<String, ? super Trader> out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Trader trader = Trader.parse(p);
                out.put(trader.getMnem(), trader);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parseViews(JsonParser p, Map<String, ? super MarketView> out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final MarketView view = MarketView.parse(p);
                out.put(view.getMarket(), view);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parseOrders(JsonParser p, Map<Long, ? super Order> out) throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Order order = Order.parse(p);
                out.put(order.getId(), order);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parseExecs(JsonParser p, Map<Long, ? super Exec> out) throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Exec exec = Exec.parse(p);
                out.put(exec.getId(), exec);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parsePosns(JsonParser p, Map<PosnKey, ? super Posn> out)
            throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Posn posn = Posn.parse(p);
                out.put(new PosnKey(posn.getContr(), posn.getSettlDay()), posn);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    private static void parseQuotes(JsonParser p, Map<Long, ? super Quote> out) throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Quote quote = Quote.parse(p);
                out.put(quote.getId(), quote);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    public static final class RecStruct {
        public final Map<String, Asset> assets = new HashMap<>();
        public final Map<String, Contr> contrs = new HashMap<>();
        public final Map<String, Market> markets = new HashMap<>();
        public final Map<String, Trader> traders = new HashMap<>();
    }

    public static final class SessStruct {
        public final Map<Long, Order> orders = new HashMap<>();
        public final Map<Long, Exec> trades = new HashMap<>();
        public final Map<PosnKey, Posn> posns = new HashMap<>();
        public final Map<Long, Quote> quotes = new HashMap<>();
    }

    public static final class TransStruct {
        public final Map<Long, Order> orders = new HashMap<>();
        public final Map<Long, Exec> execs = new HashMap<>();
        public @Nullable Posn posn;
        public @Nullable MarketView view;
    }

    private static final RecStruct parseRecStruct(JsonParser p) throws IOException {
        final RecStruct out = new RecStruct();
        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return out;
            case KEY_NAME:
                name = p.getString();
                break;
            case START_ARRAY:
                if ("assets".equals(name)) {
                    parseAssets(p, out.assets);
                } else if ("contrs".equals(name)) {
                    parseContrs(p, out.contrs);
                } else if ("markets".equals(name)) {
                    parseMarkets(p, out.markets);
                } else if ("traders".equals(name)) {
                    parseTraders(p, out.traders);
                } else {
                    throw new IOException(String.format("unexpected array field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    private static final SessStruct parseSessStruct(JsonParser p) throws IOException {
        final SessStruct out = new SessStruct();
        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return out;
            case KEY_NAME:
                name = p.getString();
                break;
            case START_ARRAY:
                if ("orders".equals(name)) {
                    parseOrders(p, out.orders);
                } else if ("trades".equals(name)) {
                    parseExecs(p, out.trades);
                } else if ("posns".equals(name)) {
                    parsePosns(p, out.posns);
                } else if ("quotes".equals(name)) {
                    parseQuotes(p, out.quotes);
                } else {
                    throw new IOException(String.format("unexpected array field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    private static final TransStruct parseTransStruct(JsonParser p) throws IOException {
        final TransStruct out = new TransStruct();
        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return out;
            case KEY_NAME:
                name = p.getString();
                break;
            case START_ARRAY:
                if ("orders".equals(name)) {
                    parseOrders(p, out.orders);
                } else if ("execs".equals(name)) {
                    parseExecs(p, out.execs);
                } else {
                    throw new IOException(String.format("unexpected array field '%s'", name));
                }
                break;
            case START_OBJECT:
                if ("posn".equals(name)) {
                    out.posn = Posn.parse(p);
                } else if ("view".equals(name)) {
                    out.view = MarketView.parse(p);
                } else {
                    throw new IOException(String.format("unexpected array field '%s'", name));
                }
                break;
            case VALUE_NULL:
                if ("posn".equals(name)) {
                    out.posn = null;
                } else {
                    throw new IOException(String.format("unexpected null field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    public BackUnrest(LockableServ serv) {
        rest = new BackRest(serv);
    }

    public BackUnrest(Model model, Journ journ, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this(new LockableServ(model, journ, cache, factory, now));
    }

    public BackUnrest(Datastore datastore, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this(new LockableServ(datastore, cache, factory, now));
    }

    public final @Nullable String findTraderByEmail(String email) {
        return rest.findTraderByEmail(email);
    }

    public final RecStruct getRec(EntitySet es, Params params, long now) throws IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getRec(es, params, now, sb);
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseRecStruct(p);
        }
    }

    @Deprecated
    public final RecStruct getRec(boolean withTraders, Params params, long now) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final EntitySet es = new EntitySet(
                EntitySet.ASSET | EntitySet.CONTR | EntitySet.MARKET | EntitySet.TRADER);
        rest.getRec(es, params, now, sb);
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseRecStruct(p);
        }
    }

    public final Map<String, Rec> getRec(RecType recType, Params params, long now)
            throws IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getRec(recType, params, now, sb);

        final Map<String, Rec> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            switch (recType) {
            case ASSET:
                parseAssets(p, out);
                break;
            case CONTR:
                parseContrs(p, out);
                break;
            case MARKET:
                parseMarkets(p, out);
                break;
            case TRADER:
                parseTraders(p, out);
                break;
            }
        }
        return out;
    }

    public final Rec getRec(RecType recType, String mnem, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getRec(recType, mnem, params, now, sb);

        Rec rec = null;
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            switch (recType) {
            case ASSET:
                rec = Asset.parse(p);
                break;
            case CONTR:
                rec = Contr.parse(p);
                break;
            case MARKET:
                rec = Market.parse(p);
                break;
            case TRADER:
                rec = Trader.parse(p);
                break;
            }
        }
        assert rec != null;
        return rec;
    }

    public final SessStruct getSess(String trader, EntitySet es, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getSess(trader, es, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseSessStruct(p);
        }
    }

    public final Map<Long, Order> getOrder(String trader, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(trader, params, now, sb);

        final Map<Long, Order> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseOrders(p, out);
        }
        return out;
    }

    public final Map<Long, Order> getOrder(String trader, String market, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(trader, market, params, now, sb);

        final Map<Long, Order> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseOrders(p, out);
        }
        return out;
    }

    public final Order getOrder(String trader, String market, long id, Params params, long now)
            throws IOException, NotFoundException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(trader, market, id, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Order.parse(p);
        }
    }

    public final Map<Long, Exec> getTrade(String trader, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getTrade(trader, params, now, sb);

        final Map<Long, Exec> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseExecs(p, out);
        }
        return out;
    }

    public final Map<Long, Exec> getTrade(String trader, String market, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getTrade(trader, market, params, now, sb);

        final Map<Long, Exec> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseExecs(p, out);
        }
        return out;
    }

    public final Exec getTrade(String trader, String market, long id, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getTrade(trader, market, id, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Exec.parse(p);
        }
    }

    public final Map<PosnKey, Posn> getPosn(String trader, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getPosn(trader, params, now, sb);

        final Map<PosnKey, Posn> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parsePosns(p, out);
        }
        return out;
    }

    public final Map<PosnKey, Posn> getPosn(String trader, String contr, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getPosn(trader, contr, params, now, sb);

        final Map<PosnKey, Posn> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parsePosns(p, out);
        }
        return out;
    }

    public final Posn getPosn(String trader, String contr, int settlDay, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getPosn(trader, contr, maybeJdToIso(settlDay), params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Posn.parse(p);
        }
    }

    public final Map<String, MarketView> getView(Params params, long now) throws IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getView(params, now, sb);

        final Map<String, MarketView> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseViews(p, out);
        }
        return out;
    }

    public final MarketView getView(String market, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getView(market, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return MarketView.parse(p);
        }
    }

    public final Trader postTrader(String mnem, @Nullable String display, String email,
            Params params, long now)
                    throws BadRequestException, ServiceUnavailableException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.postTrader(mnem, display, email, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Trader.parse(p);
        }
    }

    public final Trader putTrader(String mnem, @Nullable String display, Params params, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        final StringBuilder sb = new StringBuilder();
        rest.putTrader(mnem, display, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Trader.parse(p);
        }
    }

    public final Market postMarket(String mnem, @Nullable String display, String contr,
            int settlDate, int expiryDate, int state, Params params, long now)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        final StringBuilder sb = new StringBuilder();
        rest.postMarket(mnem, display, contr, settlDate, expiryDate, state, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Market.parse(p);
        }
    }

    public final Market putMarket(String mnem, @Nullable String display, int state, Params params,
            long now) throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        final StringBuilder sb = new StringBuilder();
        rest.putMarket(mnem, display, state, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Market.parse(p);
        }
    }

    public final void deleteOrder(String trader, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        rest.deleteOrder(trader, market, id, now);
    }

    public final void deleteOrder(String trader, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        rest.deleteOrder(trader, market, first, now);
    }

    public final TransStruct postOrder(String trader, String market, @Nullable String ref,
            Side side, long lots, long ticks, long minLots, Params params, long now)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        final StringBuilder sb = new StringBuilder();
        rest.postOrder(trader, market, ref, side, lots, ticks, minLots, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseTransStruct(p);
        }
    }

    public final TransStruct putOrder(String trader, String market, long id, long lots,
            Params params, long now) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.putOrder(trader, market, id, lots, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseTransStruct(p);
        }
    }

    public final TransStruct putOrder(String trader, String market, JslNode first, long lots,
            Params params, long now) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.putOrder(trader, market, first, lots, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseTransStruct(p);
        }
    }

    public final void deleteTrade(String trader, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        rest.deleteTrade(trader, market, id, now);
    }

    public final void deleteTrade(String trader, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        rest.deleteTrade(trader, market, first, now);
    }

    public final Exec postTrade(String trader, String market, String ref, Side side, long lots,
            long ticks, Role role, String cpty, Params params, long now)
                    throws NotFoundException, ServiceUnavailableException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.postTrade(trader, market, ref, side, lots, ticks, role, cpty, params, now, sb);
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Exec.parse(p);
        }
    }

    public final void getEndOfDay(long now) throws NotFoundException, ServiceUnavailableException {
        rest.endOfDay(now);
    }
}

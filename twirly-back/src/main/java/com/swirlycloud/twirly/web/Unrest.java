/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.parseStartArray;
import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.app.Model;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.domain.View;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.ForbiddenException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.util.Params;

public final class Unrest {

    private final Rest rest;

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

    private static void parseViews(JsonParser p, Map<Long, ? super View> out) throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final View view = View.parse(p);
                out.put(view.getId(), view);
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

    private static void parsePosns(JsonParser p, Map<Long, ? super Posn> out) throws IOException {
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case START_OBJECT:
                final Posn posn = Posn.parse(p);
                out.put(posn.getId(), posn);
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
        public final Map<String, Trader> traders = new HashMap<>();
    }

    public static final class AccntStruct {
        public final Map<Long, Order> orders = new HashMap<>();
        public final Map<Long, Exec> trades = new HashMap<>();
        public final Map<Long, Posn> posns = new HashMap<>();
    }

    public static final class TransStruct {
        public View market;
        public final Map<Long, Order> orders = new HashMap<>();
        public final Map<Long, Exec> execs = new HashMap<>();
        public Posn posn;
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

    private static final AccntStruct parseAccntStruct(JsonParser p) throws IOException {
        final AccntStruct out = new AccntStruct();
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
                if ("market".equals(name)) {
                    out.market = View.parse(p);
                } else if ("posn".equals(name)) {
                    out.posn = Posn.parse(p);
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

    public Unrest(Model model) {
        rest = new Rest(model);
    }

    public final RecStruct getRec(boolean withTraders, Params params, long now) throws IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getRec(withTraders, params, now, sb);

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
            case TRADER:
                rec = Trader.parse(p);
                break;
            }
        }
        return rec;
    }

    public final Trader postTrader(String mnem, String display, String email, Params params,
            long now) throws BadRequestException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.postTrader(mnem, display, email, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Trader.parse(p);
        }
    }

    public final Map<Long, View> getMarket(Params params, long now) throws IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getMarket(params, now, sb);

        final Map<Long, View> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseViews(p, out);
        }
        return out;
    }

    public final Map<Long, View> getMarket(String cmnem, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getMarket(cmnem, params, now, sb);

        final Map<Long, View> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseViews(p, out);
        }
        return out;
    }

    public final View getMarket(String cmnem, int settlDate, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getMarket(cmnem, settlDate, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return View.parse(p);
        }
    }

    public final View postMarket(String cmnem, int settlDate, int fixingDate, int expiryDate,
            Params params, long now) throws BadRequestException, NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.postMarket(cmnem, settlDate, fixingDate, expiryDate, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return View.parse(p);
        }
    }

    public final AccntStruct getAccnt(String email, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getAccnt(email, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseAccntStruct(p);
        }
    }

    public final void deleteOrder(String email, String cmnem, int settlDate, long id)
            throws BadRequestException, NotFoundException, IOException {
        rest.deleteOrder(email, cmnem, settlDate, id);
    }

    public final Map<Long, Order> getOrder(String email, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(email, params, now, sb);

        final Map<Long, Order> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseOrders(p, out);
        }
        return out;
    }

    public final Map<Long, Order> getOrder(String email, String cmnem, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(email, cmnem, params, now, sb);

        final Map<Long, Order> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseOrders(p, out);
        }
        return out;
    }

    public final Map<Long, Order> getOrder(String email, String cmnem, int settlDate,
            Params params, long now) throws ForbiddenException, NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(email, cmnem, settlDate, params, now, sb);

        final Map<Long, Order> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseOrders(p, out);
        }
        return out;
    }

    public final Order getOrder(String email, String cmnem, int settlDate, long id, Params params,
            long now) throws IOException, NotFoundException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(email, cmnem, settlDate, id, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Order.parse(p);
        }
    }

    public final TransStruct postOrder(String email, String cmnem, int settlDate, String ref,
            Action action, long ticks, long lots, long minLots, Params params, long now)
            throws BadRequestException, NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.postOrder(email, cmnem, settlDate, ref, action, ticks, lots, minLots, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseTransStruct(p);
        }
    }

    public final TransStruct putOrder(String email, String cmnem, int settlDate, long id,
            long lots, Params params, long now) throws BadRequestException, NotFoundException,
            IOException {
        final StringBuilder sb = new StringBuilder();
        rest.putOrder(email, cmnem, settlDate, id, lots, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return parseTransStruct(p);
        }
    }

    public final void deleteTrade(String email, String cmnem, int settlDate, long id)
            throws BadRequestException, NotFoundException {
        rest.deleteTrade(email, cmnem, settlDate, id);
    }

    public final Map<Long, Exec> getTrade(String email, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getTrade(email, params, now, sb);

        final Map<Long, Exec> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseExecs(p, out);
        }
        return out;
    }

    public final Map<Long, Exec> getTrade(String email, String cmnem, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getTrade(email, cmnem, params, now, sb);

        final Map<Long, Exec> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseExecs(p, out);
        }
        return out;
    }

    public final Map<Long, Exec> getTrade(String email, String cmnem, int settlDate, Params params,
            long now) throws ForbiddenException, NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getTrade(email, cmnem, settlDate, params, now, sb);

        final Map<Long, Exec> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parseExecs(p, out);
        }
        return out;
    }

    public final Exec getTrade(String email, String cmnem, int settlDate, long id, Params params,
            long now) throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getOrder(email, cmnem, settlDate, id, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Exec.parse(p);
        }
    }

    public final Map<Long, Posn> getPosn(String email, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getPosn(email, params, now, sb);

        final Map<Long, Posn> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parsePosns(p, out);
        }
        return out;
    }

    public final Map<Long, Posn> getPosn(String email, String cmnem, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getPosn(email, cmnem, params, now, sb);

        final Map<Long, Posn> out = new HashMap<>();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartArray(p);
            parsePosns(p, out);
        }
        return out;
    }

    public final Posn getPosn(String email, String cmnem, int settlDate, Params params, long now)
            throws NotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getPosn(email, cmnem, settlDate, params, now, sb);

        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
            return Posn.parse(p);
        }
    }

    public final void getEndOfDay() throws NotFoundException {
        rest.getEndOfDay();
    }
}

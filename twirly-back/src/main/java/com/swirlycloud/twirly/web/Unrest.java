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
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Trader;
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
                final Asset asset = Asset.parse(p, false);
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
                final Contr contr = Contr.parse(p, false);
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
                final Trader trader = Trader.parse(p, false);
                out.put(trader.getMnem(), trader);
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    public static final class RecHolder {
        public final Map<String, Asset> assets = new HashMap<>(); 
        public final Map<String, Contr> contrs = new HashMap<>(); 
        public final Map<String, Trader> traders = new HashMap<>(); 
    }

    public Unrest(Model model) {
        rest = new Rest(model);
    }

    public final RecHolder getRec(boolean withTraders, Params params, long now) throws IOException {
        final StringBuilder sb = new StringBuilder();
        rest.getRec(withTraders, params, now, sb);

        final RecHolder out = new RecHolder();
        try (JsonParser p = Json.createParser(new StringReader(sb.toString()))) {
            parseStartObject(p);
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
        }
        throw new IOException("end-of array not found");
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
            switch (recType) {
            case ASSET:
                rec = Asset.parse(p, true);
                break;
            case CONTR:
                rec = Contr.parse(p, true);
                break;
            case TRADER:
                rec = Trader.parse(p, true);
                break;
            }
        }
        return rec;
    }

    public final Object postTrader(String mnem, String display, String email, Params params,
            long now) throws BadRequestException, IOException {
        return null;
    }

    public final Object getMarket(Params params, long now) throws IOException {
        return null;
    }

    public final Object getMarket(String cmnem, Params params, long now) throws NotFoundException,
            IOException {
        return null;
    }

    public final Object getMarket(String cmnem, int settlDate, Params params, long now)
            throws NotFoundException, IOException {
        return null;
    }

    public final Object postMarket(String cmnem, int settlDate, int fixingDate, int expiryDate,
            Params params, long now) throws BadRequestException, NotFoundException, IOException {
        return null;
    }

    public final Object getAccnt(String email, Params params, long now) throws NotFoundException,
            IOException {
        return null;
    }

    public final void deleteOrder(String email, String cmnem, int settlDate, long id)
            throws BadRequestException, NotFoundException, IOException {
    }

    public final Object getOrder(String email, Params params, long now) throws NotFoundException,
            IOException {
        return null;
    }

    public final Object getOrder(String email, String cmnem, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        return null;
    }

    public final Object getOrder(String email, String cmnem, int settlDate, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        return null;
    }

    public final Object getOrder(String email, String cmnem, int settlDate, long id, Params params,
            long now) throws IOException, NotFoundException {
        return null;
    }

    public final Object postOrder(String email, String cmnem, int settlDate, String ref,
            Action action, long ticks, long lots, long minLots, Params params, long now)
            throws BadRequestException, NotFoundException, IOException {
        return null;
    }

    public final Object putOrder(String email, String cmnem, int settlDate, long id, long lots,
            Params params, long now) throws BadRequestException, NotFoundException, IOException {
        return null;
    }

    public final void deleteTrade(String email, String cmnem, int settlDate, long id)
            throws BadRequestException, NotFoundException {
    }

    public final Object getTrade(String email, Params params, long now) throws NotFoundException,
            IOException {
        return null;
    }

    public final Object getTrade(String email, String cmnem, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        return null;
    }

    public final Object getTrade(String email, String cmnem, int settlDate, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        return null;
    }

    public final Object getTrade(String email, String cmnem, int settlDate, long id, Params params,
            long now) throws NotFoundException, IOException {
        return null;
    }

    public final Object getPosn(String email, Params params, long now) throws NotFoundException,
            IOException {
        return null;
    }

    public final Object getPosn(String email, String cmnem, Params params, long now)
            throws ForbiddenException, NotFoundException, IOException {
        return null;
    }

    public final Object getPosn(String email, String cmnem, int settlDate, Params params, long now)
            throws NotFoundException, IOException {
        return null;
    }

    public final void getEndOfDay() throws NotFoundException {
    }
}

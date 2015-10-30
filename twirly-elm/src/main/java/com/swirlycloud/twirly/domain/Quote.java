/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class Quote extends BasicRequest {

    private static final long serialVersionUID = 1L;

    private final long ticks;
    long expiry;

    Quote(long id, String trader, String market, String contr, int settlDay, @Nullable String ref,
            Side side, long ticks, long lots, long created, long expiry) {
        super(id, trader, market, contr, settlDay, ref, side, lots, created);
        this.ticks = ticks;
        this.expiry = expiry;
    }

    public static Quote parse(JsonParser p) throws IOException {
        long id = 0;
        String trader = null;
        String market = null;
        String contr = null;
        int settlDay = 0;
        String ref = null;
        Side side = null;
        long ticks = 0;
        long lots = 0;
        long created = 0;
        long expiry = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                if (trader == null) {
                    throw new IOException("trader is null");
                }
                if (market == null) {
                    throw new IOException("market is null");
                }
                if (contr == null) {
                    throw new IOException("contr is null");
                }
                if (side == null) {
                    throw new IOException("side is null");
                }
                return new Quote(id, trader, market, contr, settlDay, ref, side, ticks, lots,
                        created, expiry);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NULL:
                if ("settlDate".equals(name)) {
                    settlDay = 0;
                } else if ("ref".equals(name)) {
                    ref = "";
                } else {
                    throw new IOException(String.format("unexpected null field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else if ("settlDate".equals(name)) {
                    settlDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("ticks".equals(name)) {
                    ticks = p.getLong();
                } else if ("lots".equals(name)) {
                    lots = p.getLong();
                } else if ("created".equals(name)) {
                    created = p.getLong();
                } else if ("expiry".equals(name)) {
                    expiry = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("trader".equals(name)) {
                    trader = p.getString();
                } else if ("market".equals(name)) {
                    market = p.getString();
                } else if ("contr".equals(name)) {
                    contr = p.getString();
                } else if ("ref".equals(name)) {
                    ref = p.getString();
                } else if ("side".equals(name)) {
                    final String s = p.getString();
                    assert s != null;
                    side = Side.valueOf(s);
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    @Override
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        out.append("{\"id\":").append(String.valueOf(id));
        out.append(",\"trader\":\"").append(trader);
        out.append("\",\"market\":\"").append(market);
        out.append("\",\"contr\":\"").append(contr);
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
        out.append(",\"ref\":");
        if (ref != null) {
            out.append('"').append(ref).append('"');
        } else {
            out.append("null");
        }
        out.append(",\"side\":\"").append(side.name());
        out.append("\",\"ticks\":").append(String.valueOf(ticks));
        out.append(",\"lots\":").append(String.valueOf(lots));
        out.append(",\"created\":").append(String.valueOf(created));
        out.append(",\"expiry\":").append(String.valueOf(expiry));
        out.append("}");
    }

    public final long getTicks() {
        return ticks;
    }

    public final long getExpiry() {
        return expiry;
    }
}

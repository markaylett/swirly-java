/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.IdUtil.newId;
import static com.swirlycloud.twirly.util.JsonUtil.getIdOrMnem;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.util.Identifiable;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

/**
 * A flattened view of a market.
 * 
 * @author Mark Aylett
 */
public final class View implements Identifiable, Jsonifiable {
    /**
     * Maximum price levels in view.
     */
    private static final int DEPTH_MAX = 5;

    private final transient long key;
    private Identifiable contr;
    private final int settlDay;
    private final int fixingDay;
    private final int expiryDay;
    private final Ladder ladder;
    private long lastTicks;
    private long lastLots;
    private long lastTime;

    private View(long key, Identifiable contr, int settlDay, int fixingDay, int expiryDay,
            Ladder ladder, long lastTicks, long lastLots, long lastTime) {
        assert ladder != null;
        this.key = key;
        this.contr = contr;
        this.settlDay = settlDay;
        this.fixingDay = fixingDay;
        this.expiryDay = expiryDay;
        this.ladder = ladder;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
    }

    public View(Identifiable contr, int settlDay, int fixingDay, int expiryDay, Ladder ladder,
            long lastTicks, long lastLots, long lastTime) {
        this(composeKey(contr.getId(), settlDay), contr, settlDay, fixingDay, expiryDay, ladder,
                lastTicks, lastLots, lastTime);
    }

    public static void parse(JsonParser p, Ladder ladder, int col) throws IOException {
        for (int row = 0; p.hasNext(); ++row) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case VALUE_NULL:
                ladder.setValue(row, col, 0);
                break;
            case VALUE_NUMBER:
                ladder.setValue(row, col, p.getLong());
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    public static View parse(JsonParser p) throws IOException {
        long key = 0;
        Identifiable contr = null;
        int settlDay = 0;
        int fixingDay = 0;
        int expiryDay = 0;
        final Ladder ladder = new Ladder();
        long lastTicks = 0;
        long lastLots = 0;
        long lastTime = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new View(key, contr, settlDay, fixingDay, expiryDay, ladder, lastTicks,
                        lastLots, lastTime);
            case KEY_NAME:
                name = p.getString();
                break;
            case START_ARRAY:
                if ("bidTicks".equals(name)) {
                    parse(p, ladder, Ladder.BID_TICKS);
                } else if ("bidLots".equals(name)) {
                    parse(p, ladder, Ladder.BID_LOTS);
                } else if ("bidCount".equals(name)) {
                    parse(p, ladder, Ladder.BID_COUNT);
                } else if ("offerTicks".equals(name)) {
                    parse(p, ladder, Ladder.OFFER_TICKS);
                } else if ("offerLots".equals(name)) {
                    parse(p, ladder, Ladder.OFFER_LOTS);
                } else if ("offerCount".equals(name)) {
                    parse(p, ladder, Ladder.OFFER_COUNT);
                } else {
                    throw new IOException(String.format("unexpected array field '%s'", name));
                }
                break;
            case VALUE_NULL:
                if ("lastTicks".equals(name)) {
                    lastTicks = 0;
                } else if ("lastLots".equals(name)) {
                    lastLots = 0;
                } else if ("lastTime".equals(name)) {
                    lastTime = 0;
                } else {
                    throw new IOException(String.format("unexpected null field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    key = p.getLong();
                } else if ("contr".equals(name)) {
                    contr = newId(p.getLong());
                } else if ("settlDate".equals(name)) {
                    settlDay = JulianDay.isoToJd(p.getInt());
                } else if ("fixingDate".equals(name)) {
                    fixingDay = JulianDay.isoToJd(p.getInt());
                } else if ("expiryDate".equals(name)) {
                    expiryDay = JulianDay.isoToJd(p.getInt());
                } else if ("lastTicks".equals(name)) {
                    lastTicks = p.getLong();
                } else if ("lastLots".equals(name)) {
                    lastLots = p.getLong();
                } else if ("lastTime".equals(name)) {
                    lastTime = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    /**
     * Synthetic view key.
     */

    public static long composeKey(long contrId, int settlDay) {
        // 16 bit contr-id.
        final long CONTR_MASK = (1L << 16) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final long TJD_MASK = (1L << 16) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = JulianDay.jdToTjd(settlDay);
        return ((contrId & CONTR_MASK) << 16) | (tjd & TJD_MASK);
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out) throws IOException {
        int depth = 3; // Default depth.
        if (params != null) {
            final Integer val = params.getParam("depth", Integer.class);
            if (val != null) {
                depth = val.intValue();
            }
        }
        // Round-up to minimum.
        depth = Math.max(depth, 1);
        // Round-down to maximum.
        depth = Math.min(depth, DEPTH_MAX);

        out.append("{\"id\":").append(String.valueOf(key));
        out.append(",\"contr\":").append(getIdOrMnem(contr, params));
        out.append(",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
        out.append(",\"fixingDate\":").append(String.valueOf(jdToIso(fixingDay)));
        out.append(",\"expiryDate\":").append(String.valueOf(jdToIso(expiryDay)));
        out.append(",\"bidTicks\":[");

        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidBid(i)) {
                out.append(String.valueOf(getBidTicks(i)));
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidLots\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidBid(i)) {
                out.append(String.valueOf(getBidLots(i)));
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidCount\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidBid(i)) {
                out.append(String.valueOf(getBidCount(i)));
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerTicks\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidOffer(i)) {
                out.append(String.valueOf(getOfferTicks(i)));
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerLots\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidOffer(i)) {
                out.append(String.valueOf(getOfferLots(i)));
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerCount\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidOffer(i)) {
                out.append(String.valueOf(getOfferCount(i)));
            } else {
                out.append("null");
            }
        }
        if (lastLots != 0) {
            out.append("],\"lastTicks\":").append(String.valueOf(lastTicks));
            out.append(",\"lastLots\":").append(String.valueOf(lastLots));
            out.append(",\"lastTime\":").append(String.valueOf(lastTime));
        } else {
            out.append("],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null");
        }
        out.append('}');
    }

    public final void enrich(Contr contr) {
        assert this.contr.getId() == contr.getId();
        this.contr = contr;
    }

    @Override
    public final long getId() {
        return key;
    }

    public final long getContrId() {
        return contr.getId();
    }

    public final Contr getContr() {
        return (Contr) contr;
    }

    public final int getSettlDay() {
        return settlDay;
    }

    public final int getFixingDay() {
        return fixingDay;
    }

    public final int getExpiryDay() {
        return expiryDay;
    }

    public final boolean isValidBid(int row) {
        return ladder.isValidBid(row);
    }

    public final long getBidTicks(int row) {
        return ladder.roundBidTicks(row);
    }

    public final long getBidLots(int row) {
        return ladder.roundBidLots(row);
    }

    public final long getBidCount(int row) {
        return ladder.roundBidCount(row);
    }

    public final boolean isValidOffer(int row) {
        return ladder.isValidOffer(row);
    }

    public final long getOfferTicks(int row) {
        return ladder.roundOfferTicks(row);
    }

    public final long getOfferLots(int row) {
        return ladder.roundOfferLots(row);
    }

    public final long getOfferCount(int row) {
        return ladder.roundOfferCount(row);
    }

    public final long getLastTicks() {
        return lastTicks;
    }

    public final long getLastLots() {
        return lastLots;
    }

    public final long getLastTime() {
        return lastTime;
    }
}

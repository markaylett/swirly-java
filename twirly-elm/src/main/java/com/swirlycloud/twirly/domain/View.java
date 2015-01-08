/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.JsonUtil.getIdOrMnem;

import java.io.IOException;

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

    public View(Identifiable contr, int settlDay, int fixingDay, int expiryDay, Ladder ladder,
            long lastTicks, long lastLots, long lastTime) {
        assert ladder != null;
        this.key = composeId(contr.getId(), settlDay);
        this.contr = contr;
        this.settlDay = settlDay;
        this.fixingDay = fixingDay;
        this.expiryDay = expiryDay;
        this.ladder = ladder;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
    }

    /**
     * Synthetic view key.
     */

    public static long composeId(long contrId, int settlDay) {
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

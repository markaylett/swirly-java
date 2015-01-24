/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.JsonUtil.getIdOrMnem;

import java.io.IOException;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Identifiable;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public final class Market extends BasicRbNode implements Identifiable, Jsonifiable {
    /**
     * Maximum price levels in view.
     */
    private static final int DEPTH_MAX = 5;

    private final transient long key;
    private Identifiable contr;
    private final int settlDay;
    private final int expiryDay;
    private final Side bidSide = new Side();
    private final Side offerSide = new Side();
    private long lastTicks;
    private long lastLots;
    private long lastTime;
    private long maxOrderId;
    private long maxExecId;

    private final Side getSide(Action action) {
        return action == Action.BUY ? bidSide : offerSide;
    }

    public Market(Identifiable contr, int settlDay, int expiryDay, long lastTicks, long lastLots,
            long lastTime, long maxOrderId, long maxExecId) {
        this.key = composeKey(contr.getId(), settlDay);
        this.contr = contr;
        this.settlDay = settlDay;
        this.expiryDay = expiryDay;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.maxOrderId = maxOrderId;
        this.maxExecId = maxExecId;
    }

    public Market(Identifiable contr, int settlDay, int expiryDay) {
        this(contr, settlDay, expiryDay, 0L, 0L, 0L, 0L, 0L);
    }

    /**
     * Synthetic market key.
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
        out.append(",\"expiryDate\":").append(String.valueOf(jdToIso(expiryDay)));
        out.append(",\"bidTicks\":[");

        final RbNode firstBid = bidSide.getFirstLevel();
        final RbNode firstOffer = offerSide.getFirstLevel();

        RbNode node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getTicks()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidLots\":[");
        node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getLots()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidCount\":[");
        node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getCount()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerTicks\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getTicks()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerLots\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getLots()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerCount\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getCount()));
                node = node.rbNext();
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

    public final void insertOrder(Order order) {
        getSide(order.getAction()).insertOrder(order);
    }

    public final void removeOrder(Order order) {
        getSide(order.getAction()).removeOrder(order);
    }

    public final void placeOrder(Order order, long now) {
        getSide(order.getAction()).placeOrder(order, now);
    }

    public final void reviseOrder(Order order, long lots, long now) {
        getSide(order.getAction()).reviseOrder(order, lots, now);
    }

    public final void cancelOrder(Order order, long now) {
        getSide(order.getAction()).cancelOrder(order, now);
    }

    public final void takeOrder(Order order, long lots, long now) {
        final Side side = getSide(order.getAction());
        side.takeOrder(order, lots, now);
        lastTicks = side.getLastTicks();
        lastLots = side.getLastLots();
        lastTime = side.getLastTime();
    }

    public final long allocOrderId() {
        return ++maxOrderId;
    }

    public final long allocExecId() {
        return ++maxExecId;
    }

    @Override
    public final long getKey() {
        return key;
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

    public final int getExpiryDay() {
        return expiryDay;
    }

    public final Side getBidSide() {
        return bidSide;
    }

    public final Side getOfferSide() {
        return offerSide;
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

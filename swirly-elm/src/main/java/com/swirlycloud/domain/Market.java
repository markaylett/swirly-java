/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.util.Date.jdToIso;

import java.io.IOException;

import com.swirlycloud.util.AshUtil;
import com.swirlycloud.util.BasicRbNode;
import com.swirlycloud.util.Date;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.Jsonifiable;
import com.swirlycloud.util.RbNode;

public final class Market extends BasicRbNode implements Identifiable, Jsonifiable {
    /**
     * Maximum price levels in view.
     */
    private static final int DEPTH_MAX = 5;

    private final long key;
    private Identifiable contr;
    private final int settlDay;
    private final Side bidSide = new Side();
    private final Side offerSide = new Side();
    private long maxOrderId;
    private long maxExecId;

    private static String getRecMnem(Identifiable iden) {
        return iden instanceof Rec ? ((Rec) iden).getMnem() : String.valueOf(iden.getId());
    }

    private final Side side(Action action) {
        return action == Action.BUY ? bidSide : offerSide;
    }

    private final void toJsonTob(Appendable out) throws IOException {
        out.append("{\"id\":").append(String.valueOf(key));
        out.append(",\"contr\":\"").append(getRecMnem(contr));
        out.append("\",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));

        final Level firstBid = (Level) bidSide.getFirstLevel();
        if (firstBid != null) {
            out.append(",\"bidTicks\":").append(String.valueOf(firstBid.getTicks()));
            out.append(",\"bidLots\":").append(String.valueOf(firstBid.getLots()));
            out.append(",\"bidCount\":").append(String.valueOf(firstBid.getCount()));
        } else {
            out.append(",\"bidTicks\":null,\"bidLots\":null,\"bidCount\":null");
        }
        final Level firstOffer = (Level) offerSide.getFirstLevel();
        if (firstOffer != null) {
            out.append(",\"offerTicks\":").append(String.valueOf(firstOffer.getTicks()));
            out.append(",\"offerLots\":").append(String.valueOf(firstOffer.getLots()));
            out.append(",\"offerCount\":").append(String.valueOf(firstOffer.getCount()));
        } else {
            out.append(",\"offerTicks\":null,\"offerLots\":null,\"offerCount\":null");
        }
        out.append("}");
    }

    private final void toJsonDepth(Appendable out, int levels) throws IOException {
        out.append("{\"id\":").append(String.valueOf(key));
        out.append(",\"contr\":\"").append(getRecMnem(contr));
        out.append("\",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
        out.append(",\"bidTicks\":[");

        final RbNode firstBid = bidSide.getFirstLevel();
        final RbNode firstOffer = offerSide.getFirstLevel();

        RbNode node = firstBid;
        for (int i = 0; i < levels; ++i) {
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
        for (int i = 0; i < levels; ++i) {
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
        for (int i = 0; i < levels; ++i) {
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
        for (int i = 0; i < levels; ++i) {
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
        for (int i = 0; i < levels; ++i) {
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
        for (int i = 0; i < levels; ++i) {
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
        out.append("]}");
    }

    public Market(Identifiable contr, int settlDay, long maxOrderId, long maxExecId) {
        this.key = composeId(contr.getId(), settlDay);
        this.contr = contr;
        this.settlDay = settlDay;
        this.maxOrderId = maxOrderId;
        this.maxExecId = maxExecId;
    }

    public Market(Identifiable contr, int settlDay) {
        this(contr, settlDay, 0L, 0L);
    }

    /**
     * Synthetic market key.
     */

    public static long composeId(long contrId, int settlDay) {
        // 16 bit contr-id.
        final long CONTR_MASK = (1L << 16) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final long TJD_MASK = (1L << 16) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = Date.jdToTjd(settlDay);
        return ((contrId & CONTR_MASK) << 16) | (tjd & TJD_MASK);
    }

    @Override
    public final String toString() {
        return AshUtil.toJson(this, null);
    }

    @Override
    public final void toJson(Appendable out, Object arg) throws IOException {
        int levels = 1;
        if (arg != null) {
            levels = (Integer) arg;
        }
        // Round-up to minimum.
        levels = Math.max(levels, 1);
        // Round-down to maximum.
        levels = Math.min(levels, DEPTH_MAX);
        if (levels == 1) {
            toJsonTob(out);
        } else {
            toJsonDepth(out, levels);
        }
    }

    public final void enrich(Contr contr) {
        assert this.contr.getId() == contr.getId();
        this.contr = contr;
    }

    public final void insertOrder(Order order) {
        side(order.getAction()).insertOrder(order);
    }

    public final void removeOrder(Order order) {
        side(order.getAction()).removeOrder(order);
    }

    public final void placeOrder(Order order, long now) {
        side(order.getAction()).placeOrder(order, now);
    }

    public final void reviseOrder(Order order, long lots, long now) {
        side(order.getAction()).reviseOrder(order, lots, now);
    }

    public final void cancelOrder(Order order, long now) {
        side(order.getAction()).cancelOrder(order, now);
    }

    public final void takeOrder(Order order, long lots, long now) {
        side(order.getAction()).takeOrder(order, lots, now);
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

    public final Side getBidSide() {
        return bidSide;
    }

    public final Side getOfferSide() {
        return offerSide;
    }
}

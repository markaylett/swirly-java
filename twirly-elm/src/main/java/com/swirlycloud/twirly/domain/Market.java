/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.MnemUtil.newMnem;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Params;

/**
 * A place where buyers and sellers come together to exchange goods or services.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class Market extends Rec implements Financial {

    private static final long serialVersionUID = 1L;
    /**
     * Maximum price levels in view.
     */
    private static final int DEPTH_MAX = 5;

    private Memorable contr;
    private final int settlDay;
    private final int expiryDay;
    private int state;
    // Two sides constitute the book.
    private final transient Side bidSide = new Side();
    private final transient Side offerSide = new Side();
    private long lastTicks;
    private long lastLots;
    private long lastTime;
    private transient long maxOrderId;
    private transient long maxExecId;

    private final Side getSide(Action action) {
        return action == Action.BUY ? bidSide : offerSide;
    }

    private Market(String mnem, @Nullable String display, Memorable contr, int settlDay,
            int expiryDay, int state) {
        this(mnem, display, contr, settlDay, expiryDay, state, 0L, 0L, 0L, 0L, 0L);
    }

    Market(String mnem, @Nullable String display, Memorable contr, int settlDay,
            int expiryDay, int state, long lastTicks, long lastLots, long lastTime,
            long maxOrderId, long maxExecId) {
        super(mnem, display);
        assert (settlDay == 0) == (expiryDay == 0);
        this.contr = contr;
        this.settlDay = settlDay;
        this.expiryDay = expiryDay;
        this.state = state;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.maxOrderId = maxOrderId;
        this.maxExecId = maxExecId;
    }

    public static Market parse(JsonParser p) throws IOException {
        String mnem = null;
        String display = null;
        Memorable contr = null;
        int settlDay = 0;
        int expiryDay = 0;
        int state = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                if (mnem == null) {
                    throw new IOException("mnem is null");
                }
                if (contr == null) {
                    throw new IOException("contr is null");
                }
                return new Market(mnem, display, contr, settlDay, expiryDay, state);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NULL:
                if ("settlDate".equals(name)) {
                    settlDay = 0;
                } else if ("expiryDate".equals(name)) {
                    expiryDay = 0;
                }
                break;
            case VALUE_NUMBER:
                if ("settlDate".equals(name)) {
                    settlDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("expiryDate".equals(name)) {
                    expiryDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("state".equals(name)) {
                    state = p.getInt();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    display = p.getString();
                } else if ("contr".equals(name)) {
                    final String s = p.getString();
                    assert s != null;
                    contr = newMnem(s);
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
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"contr\":\"").append(contr.getMnem());
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
        out.append(",\"expiryDate\":");
        if (expiryDay != 0) {
            out.append(String.valueOf(jdToIso(expiryDay)));
        } else {
            out.append("null");
        }
        out.append(",\"state\":").append(String.valueOf(state));
        out.append('}');
    }

    public final void toJsonView(@Nullable Params params, Appendable out) throws IOException {
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

        out.append("{\"market\":\"").append(mnem);
        out.append("\",\"contr\":\"").append(contr.getMnem());
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
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
        assert this.contr.getMnem().equals(contr.getMnem());
        this.contr = contr;
    }

    public final long allocOrderId() {
        return ++maxOrderId;
    }

    public final long allocExecId() {
        return ++maxExecId;
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
        lastTicks = order.getTicks();
        lastLots = lots;
        lastTime = now;
    }

    public final void setState(int state) {
        this.state = state;
    }

    @Override
    public final RecType getRecType() {
        return RecType.MARKET;
    }

    @Override
    public final String getMarket() {
        return mnem;
    }

    @Override
    public final String getContr() {
        return contr.getMnem();
    }

    public final Contr getContrRich() {
        return (Contr) contr;
    }

    @Override
    public final int getSettlDay() {
        return settlDay;
    }

    @Override
    public final boolean isSettlDaySet() {
        return settlDay != 0;
    }

    /**
     * @return the market expiry-day.
     */
    public final int getExpiryDay() {
        return expiryDay;
    }

    public final boolean isExpiryDaySet() {
        return expiryDay != 0;
    }

    public final int getState() {
        return state;
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

    public final long getMaxOrderId() {
        return maxOrderId;
    }

    public final long getMaxExecId() {
        return maxExecId;
    }
}

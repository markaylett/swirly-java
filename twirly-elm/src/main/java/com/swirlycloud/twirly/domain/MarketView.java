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
import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Params;

/**
 * A flattened view of a market.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class MarketView extends BasicRbNode implements Jsonifiable,
        Memorable, Financial {

    private static final long serialVersionUID = 1L;

    /**
     * Maximum price levels in view.
     */
    private static final int DEPTH_MAX = 5;

    private final String market;
    private final String contr;
    private final int settlDay;
    long lastTicks;
    long lastLots;
    long lastTime;
    final Ladder ladder;

    private static void parseArray(JsonParser p, Ladder ladder, int col) throws IOException {
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

    public MarketView(String market, String contr, int settlDay, long lastTicks, long lastLots,
            long lastTime, Ladder ladder) {
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.ladder = ladder;
    }

    public MarketView(Financial fin, long lastTicks, long lastLots, long lastTime, Ladder ladder) {
        this.market = fin.getMarket();
        this.contr = fin.getContr();
        this.settlDay = fin.getSettlDay();
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.ladder = ladder;
    }

    public MarketView(String market, String contr, int settlDay, long lastTicks, long lastLots,
            long lastTime) {
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.ladder = new Ladder();
    }

    public MarketView(Financial fin, long lastTicks, long lastLots, long lastTime) {
        this.market = fin.getMarket();
        this.contr = fin.getContr();
        this.settlDay = fin.getSettlDay();
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.ladder = new Ladder();
    }

    public static MarketView parse(JsonParser p) throws IOException {
        String market = null;
        String contr = null;
        int settlDay = 0;
        long lastTicks = 0;
        long lastLots = 0;
        long lastTime = 0;
        final Ladder ladder = new Ladder();

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                if (market == null) {
                    throw new IOException("market is null");
                }
                if (contr == null) {
                    throw new IOException("contr is null");
                }
                return new MarketView(market, contr, settlDay, lastTicks, lastLots, lastTime,
                        ladder);
            case KEY_NAME:
                name = p.getString();
                break;
            case START_ARRAY:
                if ("bidTicks".equals(name)) {
                    parseArray(p, ladder, Ladder.BID_TICKS);
                } else if ("bidLots".equals(name)) {
                    parseArray(p, ladder, Ladder.BID_LOTS);
                } else if ("bidCount".equals(name)) {
                    parseArray(p, ladder, Ladder.BID_COUNT);
                } else if ("offerTicks".equals(name)) {
                    parseArray(p, ladder, Ladder.OFFER_TICKS);
                } else if ("offerLots".equals(name)) {
                    parseArray(p, ladder, Ladder.OFFER_LOTS);
                } else if ("offerCount".equals(name)) {
                    parseArray(p, ladder, Ladder.OFFER_COUNT);
                } else {
                    throw new IOException(String.format("unexpected array field '%s'", name));
                }
                break;
            case VALUE_NULL:
                if ("settlDate".equals(name)) {
                    settlDay = 0;
                } else if ("lastTicks".equals(name)) {
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
                if ("settlDate".equals(name)) {
                    settlDay = JulianDay.maybeIsoToJd(p.getInt());
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
            case VALUE_STRING:
                if ("market".equals(name)) {
                    market = p.getString();
                } else if ("contr".equals(name)) {
                    contr = p.getString();
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
    public final int hashCode() {
        return market.hashCode();
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
        final MarketView other = (MarketView) obj;
        if (!market.equals(other.market)) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
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

        out.append("{\"market\":\"").append(market);
        out.append("\",\"contr\":\"").append(contr);
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
        if (lastLots != 0) {
            out.append(",\"lastTicks\":").append(String.valueOf(lastTicks));
            out.append(",\"lastLots\":").append(String.valueOf(lastLots));
            out.append(",\"lastTime\":").append(String.valueOf(lastTime));
        } else {
            out.append(",\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null");
        }
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
        out.append("]}");
    }

    @Override
    public final String getMnem() {
        return market;
    }

    @Override
    public final String getMarket() {
        return market;
    }

    @Override
    public final String getContr() {
        return contr;
    }

    @Override
    public final int getSettlDay() {
        return settlDay;
    }

    @Override
    public final boolean isSettlDaySet() {
        return settlDay != 0;
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
}

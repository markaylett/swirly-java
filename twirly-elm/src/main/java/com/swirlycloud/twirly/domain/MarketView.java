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
import com.swirlycloud.twirly.node.AbstractRbNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Params;

/**
 * A flattened view of a market.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class MarketView extends AbstractRbNode
        implements Jsonifiable, Memorable, Financial {

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
    final MarketData data;

    private static void parseArray(JsonParser p, MarketData data, int col) throws IOException {
        for (int row = 0; p.hasNext(); ++row) {
            final Event event = p.next();
            switch (event) {
            case END_ARRAY:
                return;
            case VALUE_NULL:
                data.setValue(row, col, 0);
                break;
            case VALUE_NUMBER:
                data.setValue(row, col, p.getLong());
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of array not found");
    }

    public MarketView(String market, String contr, int settlDay, long lastTicks, long lastLots,
            long lastTime, MarketData data) {
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.data = data;
    }

    public MarketView(Financial fin, long lastTicks, long lastLots, long lastTime,
            MarketData data) {
        this.market = fin.getMarket();
        this.contr = fin.getContr();
        this.settlDay = fin.getSettlDay();
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.data = data;
    }

    public MarketView(String market, String contr, int settlDay, long lastTicks, long lastLots,
            long lastTime) {
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.data = new MarketData();
    }

    public MarketView(Financial fin, long lastTicks, long lastLots, long lastTime) {
        this.market = fin.getMarket();
        this.contr = fin.getContr();
        this.settlDay = fin.getSettlDay();
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.data = new MarketData();
    }

    public static MarketView parse(JsonParser p) throws IOException {
        String market = null;
        String contr = null;
        int settlDay = 0;
        long lastTicks = 0;
        long lastLots = 0;
        long lastTime = 0;
        final MarketData data = new MarketData();

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
                        data);
            case KEY_NAME:
                name = p.getString();
                break;
            case START_ARRAY:
                if ("bidTicks".equals(name)) {
                    parseArray(p, data, MarketData.BID_TICKS);
                } else if ("bidResd".equals(name)) {
                    parseArray(p, data, MarketData.BID_RESD);
                } else if ("bidQuot".equals(name)) {
                    parseArray(p, data, MarketData.BID_QUOT);
                } else if ("bidCount".equals(name)) {
                    parseArray(p, data, MarketData.BID_COUNT);
                } else if ("offerTicks".equals(name)) {
                    parseArray(p, data, MarketData.OFFER_TICKS);
                } else if ("offerResd".equals(name)) {
                    parseArray(p, data, MarketData.OFFER_RESD);
                } else if ("offerQuot".equals(name)) {
                    parseArray(p, data, MarketData.OFFER_QUOT);
                } else if ("offerCount".equals(name)) {
                    parseArray(p, data, MarketData.OFFER_COUNT);
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
        out.append("],\"bidResd\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidBid(i)) {
                out.append(String.valueOf(getBidResd(i)));
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidQuot\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidBid(i)) {
                out.append(String.valueOf(getBidQuot(i)));
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
        out.append("],\"offerResd\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidOffer(i)) {
                out.append(String.valueOf(getOfferResd(i)));
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerQuot\":[");
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (isValidOffer(i)) {
                out.append(String.valueOf(getOfferQuot(i)));
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
        return data.isValidBid(row);
    }

    public final long getBidTicks(int row) {
        return data.getBidTicks(row);
    }

    public final long getBidResd(int row) {
        return data.getBidResd(row);
    }

    public final long getBidQuot(int row) {
        return data.getBidQuot(row);
    }

    public final long getBidCount(int row) {
        return data.getBidCount(row);
    }

    public final boolean isValidOffer(int row) {
        return data.isValidOffer(row);
    }

    public final long getOfferTicks(int row) {
        return data.getOfferTicks(row);
    }

    public final long getOfferResd(int row) {
        return data.getOfferResd(row);
    }

    public final long getOfferQuot(int row) {
        return data.getOfferQuot(row);
    }

    public final long getOfferCount(int row) {
        return data.getOfferCount(row);
    }
}

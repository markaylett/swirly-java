/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public final class Posn extends BasicRbNode implements Jsonifiable, SlNode {

    private transient SlNode next;

    private final String trader;
    private final String contr;
    private final int settlDay;
    private long buyCost;
    private long buyLots;
    private long sellCost;
    private long sellLots;

    private Posn(String trader, String contr, int settlDay, long buyCost,
            long buyLots, long sellCost, long sellLots) {
        this.trader = trader;
        this.contr = contr;
        this.settlDay = settlDay;
        this.buyCost = buyCost;
        this.buyLots = buyLots;
        this.sellCost = sellCost;
        this.sellLots = sellLots;
    }

    public Posn(String trader, String contr, int settlDay) {
        this.trader = trader;
        this.contr = contr;
        this.settlDay = settlDay;
    }

    public static Posn parse(JsonParser p) throws IOException {
        String trader = null;
        String contr = null;
        int settlDay = 0;
        long buyCost = 0;
        long buyLots = 0;
        long sellCost = 0;
        long sellLots = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Posn(trader, contr, settlDay, buyCost, buyLots, sellCost,
                        sellLots);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("settlDate".equals(name)) {
                    settlDay = JulianDay.isoToJd(p.getInt());
                } else if ("buyCost".equals(name)) {
                    buyCost = p.getLong();
                } else if ("buyLots".equals(name)) {
                    buyLots = p.getLong();
                } else if ("sellCost".equals(name)) {
                    sellCost = p.getLong();
                } else if ("sellLots".equals(name)) {
                    sellLots = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("trader".equals(name)) {
                    trader = p.getString();
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
        final int prime = 31;
        int result = 1;
        result = prime * result + trader.hashCode();
        result = prime * result + contr.hashCode();
        result = prime * result + settlDay;
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Posn other = (Posn) obj;
        if (!trader.equals(other.trader)) {
            return false;
        }
        if (!contr.equals(other.contr)) {
            return false;
        }
        if (settlDay == other.settlDay) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out) throws IOException {
        out.append("{\"trader\":\"").append(trader);
        out.append("\",\"contr\":\"").append(contr);
        out.append("\",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
        if (buyLots != 0) {
            out.append(",\"buyCost\":").append(String.valueOf(buyCost));
            out.append(",\"buyLots\":").append(String.valueOf(buyLots));
        } else {
            out.append(",\"buyCost\":0,\"buyLots\":0");
        }
        if (sellLots != 0) {
            out.append(",\"sellCost\":").append(String.valueOf(sellCost));
            out.append(",\"sellLots\":").append(String.valueOf(sellLots));
        } else {
            out.append(",\"sellCost\":0,\"sellLots\":0");
        }
        out.append("}");
    }

    @Override
    public final void setSlNext(SlNode next) {
        this.next = next;
    }

    @Override
    public final SlNode slNext() {
        return next;
    }

    public final void applyTrade(Action action, long lastTicks, long lastLots) {
        final double cost = lastLots * lastTicks;
        if (action == Action.BUY) {
            buyCost += cost;
            buyLots += lastLots;
        } else {
            assert action == Action.SELL;
            sellCost += cost;
            sellLots += lastLots;
        }
    }

    public final void applyTrade(Exec trade) {
        applyTrade(trade.getAction(), trade.getLastTicks(), trade.getLastLots());
    }

    public final void setBuyCost(long buyCost) {
        this.buyCost = buyCost;
    }

    public final void setBuyLots(long buyLots) {
        this.buyLots = buyLots;
    }

    public final void setSellCost(long sellCost) {
        this.sellCost = sellCost;
    }

    public final void setSellLots(long sellLots) {
        this.sellLots = sellLots;
    }

    public final String getTrader() {
        return trader;
    }

    public final String getContr() {
        return contr;
    }

    public final int getSettlDay() {
        return settlDay;
    }

    public final long getBuyCost() {
        return buyCost;
    }

    public final long getBuyLots() {
        return buyLots;
    }

    public final long getSellCost() {
        return sellCost;
    }

    public final long getSellLots() {
        return sellLots;
    }
}

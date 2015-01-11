/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.JsonUtil.getIdOrMnem;

import java.io.IOException;

import com.swirlycloud.twirly.collection.BasicRbNode;
import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.util.Identifiable;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public final class Posn extends BasicRbNode implements Identifiable, Jsonifiable {

    private final transient long key;
    private Identifiable trader;
    private Identifiable contr;
    private final int settlDay;
    private long buyCost;
    private long buyLots;
    private long sellCost;
    private long sellLots;

    public Posn(Identifiable trader, Identifiable contr, int settlDay) {
        this.key = composeKey(contr.getId(), settlDay, trader.getId());
        this.trader = trader;
        this.contr = contr;
        this.settlDay = settlDay;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out)
            throws IOException {
        out.append("{\"id\":").append(String.valueOf(key));
        out.append(",\"trader\":").append(getIdOrMnem(trader, params));
        out.append(",\"contr\":").append(getIdOrMnem(contr, params));
        out.append(",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
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

    public final void enrich(Trader trader, Contr contr) {
        assert this.trader.getId() == trader.getId();
        assert this.contr.getId() == contr.getId();
        this.trader = trader;
        this.contr = contr;
    }

    /**
     * Synthetic position key.
     */

    public static long composeKey(long contrId, int settlDay, long traderId) {
        // 16 bit contr-id.
        final long CONTR_MASK = (1L << 16) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final long TJD_MASK = (1L << 16) - 1;
        // 32 bit trader-id.
        final long TRADER_MASK = (1L << 32) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = JulianDay.jdToTjd(settlDay);
        return ((contrId & CONTR_MASK) << 48) | ((tjd & TJD_MASK) << 32) | (traderId & TRADER_MASK);
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

    @Override
    public final long getKey() {
        return key;
    }

    @Override
    public final long getId() {
        return key;
    }

    public final long getTraderId() {
        return trader.getId();
    }

    public final Trader getTrader() {
        return (Trader) trader;
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

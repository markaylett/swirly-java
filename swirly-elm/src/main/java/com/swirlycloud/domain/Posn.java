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

public final class Posn extends BasicRbNode implements Identifiable, Jsonifiable {

    private final long key;
    private Identifiable user;
    private Identifiable contr;
    private final int settlDay;
    private long buyLicks;
    private long buyLots;
    private long sellLicks;
    private long sellLots;

    private static String getRecMnem(Identifiable iden) {
        return iden instanceof Rec ? ((Rec) iden).mnem : String.valueOf(iden.getId());
    }

    public Posn(Identifiable user, Identifiable contr, int settlDay) {
        this.key = composeId(contr.getId(), settlDay, user.getId());
        this.user = user;
        this.contr = contr;
        this.settlDay = settlDay;
    }

    @Override
    public final String toString() {
        return AshUtil.toJson(this, null);
    }

    @Override
    public final void toJson(Appendable out, Object arg) throws IOException {
        out.append("{\"id\":").append(String.valueOf(key));
        out.append(",\"user\":\"").append(getRecMnem(user));
        out.append("\",\"contr\":\"").append(getRecMnem(contr));
        out.append("\",\"settlDate\":").append(String.valueOf(jdToIso(settlDay)));
        if (buyLots != 0) {
            out.append(",\"buyLicks\":").append(String.valueOf(buyLicks));
            out.append(",\"buyLots\":").append(String.valueOf(buyLots));
        } else {
            out.append(",\"buyLicks\":0,\"buyLots\":0");
        }
        if (sellLots != 0) {
            out.append(",\"sellLicks\":").append(String.valueOf(sellLicks));
            out.append(",\"sellLots\":").append(String.valueOf(sellLots));
        } else {
            out.append(",\"sellLicks\":0,\"sellLots\":0");
        }
        out.append("}");
    }

    public final void enrich(User user, Contr contr) {
        assert this.user.getId() == user.getId();
        assert this.contr.getId() == contr.getId();
        this.user = user;
        this.contr = contr;
    }

    /**
     * Synthetic position key.
     */

    public static long composeId(long contrId, int settlDay, long userId) {
        // 16 bit contr-id.
        final long CONTR_MASK = (1L << 16) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final long TJD_MASK = (1L << 16) - 1;
        // 32 bit user-id.
        final long USER_MASK = (1L << 32) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = Date.jdToTjd(settlDay);
        return ((contrId & CONTR_MASK) << 48) | ((tjd & TJD_MASK) << 32) | (userId & USER_MASK);
    }

    public final void applyTrade(Action action, long lastTicks, long lastLots) {
        final double licks = lastLots * lastTicks;
        if (action == Action.BUY) {
            buyLicks += licks;
            buyLots += lastLots;
        } else {
            assert action == Action.SELL;
            sellLicks += licks;
            sellLots += lastLots;
        }
    }

    public final void applyTrade(Exec trade) {
        applyTrade(trade.getAction(), trade.getLastTicks(), trade.getLastLots());
    }

    public final void setBuyLicks(long buyLicks) {
        this.buyLicks = buyLicks;
    }

    public final void setBuyLots(long buyLots) {
        this.buyLots = buyLots;
    }

    public final void setSellLicks(long sellLicks) {
        this.sellLicks = sellLicks;
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

    public final long getUserId() {
        return user.getId();
    }

    public final User getUser() {
        return (User) user;
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

    public final long getBuyLicks() {
        return buyLicks;
    }

    public final long getBuyLots() {
        return buyLots;
    }

    public final long getSellLicks() {
        return sellLicks;
    }

    public final long getSellLots() {
        return sellLots;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import static org.doobry.util.Date.jdToIso;

import org.doobry.util.BasicRbNode;
import org.doobry.util.Date;
import org.doobry.util.Identifiable;
import org.doobry.util.Printable;

public final class Posn extends BasicRbNode implements Printable {

    private final long key;
    private Identifiable party;
    private Identifiable contr;
    private final int settlDay;
    private long buyLicks;
    private long buyLots;
    private long sellLicks;
    private long sellLots;

    private static String getRecMnem(Identifiable iden) {
        return iden instanceof Rec ? ((Rec) iden).mnem : String.valueOf(iden.getId());
    }

    public Posn(Identifiable party, Identifiable contr, int settlDay) {
        this.key = toKey(party.getId(), contr.getId(), settlDay);
        this.party = party;
        this.contr = contr;
        this.settlDay = settlDay;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        print(sb);
        return sb.toString();
    }

    @Override
    public final void print(StringBuilder sb) {
        sb.append("{\"accnt\":\"").append(getRecMnem(party));
        sb.append("\",\"contr\":\"").append(getRecMnem(contr));
        sb.append("\",\"settl_date\":").append(jdToIso(settlDay));
        sb.append(",\"buy_licks\":").append(buyLicks);
        sb.append(",\"buy_lots\":").append(buyLots);
        sb.append(",\"sell_licks\":").append(sellLicks);
        sb.append(",\"sell_lots\":").append(sellLots);
        sb.append("}");
    }

    public final void enrich(Party party, Contr contr) {
        assert this.party.getId() == party.getId();
        assert this.contr.getId() == contr.getId();
        this.party = party;
        this.contr = contr;
    }

    /**
     * Synthetic position key.
     */

    public static long toKey(long aid, long cid, int settlDay) {
        // 16 million ids.
        final int ID_MASK = (1 << 24) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final int JD_MASK = (1 << 16) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = Date.jdToTjd(settlDay);
        return ((aid & ID_MASK) << 40) | ((cid & ID_MASK) << 16) | (tjd & JD_MASK);
    }

    public final void applyTrade(Exec trade) {
        final double licks = trade.getLastLots() * trade.getLastTicks();
        if (trade.getAction() == Action.BUY) {
            buyLicks += licks;
            buyLots += trade.getLastLots();
        } else {
            assert trade.getAction() == Action.SELL;
            sellLicks += licks;
            sellLots += trade.getLastLots();
        }
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

    public final long getKey() {
        return key;
    }

    public final long getPartyId() {
        return party.getId();
    }

    public final Party getParty() {
        return (Party) party;
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

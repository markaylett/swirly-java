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
import org.doobry.util.RbNode;

public final class Book extends BasicRbNode implements Identifiable, Printable {

    /**
     * Maximum price levels in view.
     */
    private static final int LEVEL_MAX = 5;

    private final long id;
    private final Contr contr;
    private final int settlDay;
    private final Side bidSide = new Side();
    private final Side offerSide = new Side();

    private final Side side(Action action) {
        return action == Action.BUY ? bidSide : offerSide;
    }

    public Book(Contr contr, int settlDay) {
        this.id = toKey(contr.getId(), settlDay);
        this.contr = contr;
        this.settlDay = settlDay;
    }

    /**
     * Synthetic book key.
     */

    public static long toKey(long cid, int settlDay) {
        // 16 million ids.
        final int ID_MASK = (1 << 24) - 1;
        // 16 bits is sufficient for truncated Julian day.
        final int JD_MASK = (1 << 16) - 1;

        // Truncated Julian Day (TJD).
        final long tjd = Date.jdToTjd(settlDay);
        return ((cid & ID_MASK) << 16) | (tjd & JD_MASK);
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        print(sb);
        return sb.toString();
    }

    @Override
    public final void print(StringBuilder sb) {
        sb.append("{\"contr\":\"").append(contr.getMnem()).append("\",");
        sb.append("\"settlDate\":").append(jdToIso(settlDay)).append(",");

        final RbNode bidFirst = bidSide.getFirstLevel();
        final RbNode offerFirst = offerSide.getFirstLevel();

        sb.append("\"bidTicks\":[");
        RbNode node = bidFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getTicks());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"bidLots\":[");
        node = bidFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getLots());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"bidCount\":[");
        node = bidFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getCount());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"offerTicks\":[");
        node = offerFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getTicks());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"offerLots\":[");
        node = offerFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getLots());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"offerCount\":[");
        node = offerFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getCount());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("]}");
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

    @Override
    public final long getKey() {
        return id;
    }

    @Override
    public final long getId() {
        return id;
    }

    public final Contr getContr() {
        return contr;
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

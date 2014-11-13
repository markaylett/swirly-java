/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Action;
import org.doobry.domain.Contr;
import org.doobry.domain.Order;
import org.doobry.domain.Side;
import org.doobry.util.BasicRbNode;
import org.doobry.util.Date;

public final class Book extends BasicRbNode {
    private final long key;
    private final Contr contr;
    private final int settlDay;
    private final Side bidSide = new Side();
    private final Side offerSide = new Side();

    private final Side side(Action action) {
        return action == Action.BUY ? bidSide : offerSide;
    }

    public Book(Contr contr, int settlDay) {
        this.key = toKey(contr.getId(), settlDay);
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
        return key;
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

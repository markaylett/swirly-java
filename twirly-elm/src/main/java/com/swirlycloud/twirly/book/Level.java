/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.book;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.entity.Order;
import com.swirlycloud.twirly.entity.Quote;
import com.swirlycloud.twirly.node.AbstractRbNode;
import com.swirlycloud.twirly.node.DlNode;

/**
 * Price level.
 * 
 * A price level is an aggregation of orders by price. I.e. the sum of all orders in the book at the
 * same price.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class Level extends AbstractRbNode {

    private static final long serialVersionUID = 1L;

    private final long key;
    private final long ticks;
    Order firstOrder;
    /**
     * Must be greater than zero.
     */
    long resd;
    private long quotd;
    /**
     * Must be greater than zero.
     */
    int count;

    public Level(Order order) {
        final long ticks = order.getTicks();
        this.key = composeKey(order.getSide(), ticks);
        this.ticks = ticks;
        this.firstOrder = order;
        this.resd = order.getResd();
        this.quotd = order.getQuotd();
        this.count = 1;
    }

    /**
     * Synthetic level key.
     */
    public static long composeKey(Side side, long ticks) {
        return side == Side.BUY ? -ticks : ticks;
    }

    public final long getKey() {
        return key;
    }

    public final void addOrder(Order order) {
        resd += order.getResd();
        quotd += order.getQuotd();
        ++count;
    }

    public final void subOrder(Order order) {
        resd -= order.getResd();
        quotd -= order.getQuotd();
        --count;
    }

    public final void addQuote(Quote quote) {
        quotd += quote.getLots();
    }

    public final void subQuote(Quote quote) {
        quotd -= quote.getLots();
    }

    public final long getTicks() {
        return ticks;
    }

    public final DlNode getFirstOrder() {
        return firstOrder;
    }

    public final long getResd() {
        return resd;
    }

    public final long getQuotd() {
        return quotd;
    }

    public final int getCount() {
        return count;
    }

    public final long getAvail() {
        return resd - quotd;
    }
}
/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.node.AbstractJslNode;

public final @NonNullByDefault class MarketId extends AbstractJslNode {

    private final String market;
    private final long id;

    public MarketId(String market, long id) {
        this.market = market;
        this.id = id;
    }

    /**
     * Parse comma-delimited string.
     * 
     * @param ids
     *            Comma-delimited string.
     * @return Linked-list.
     */
    public static @Nullable MarketId parse(String market, String ids) {
        MarketId firstMid = null;
        int i = 0, j = 0;
        for (; j < ids.length(); ++j) {
            if (ids.charAt(j) == ',') {
                final MarketId mid = new MarketId(market, Long.valueOf(ids.substring(i, j)));
                mid.setJslNext(firstMid);
                firstMid = mid;
                i = j + 1;
            }
        }
        if (i != j) {
            final MarketId mid = new MarketId(market, Long.valueOf(ids.substring(i, j)));
            mid.setJslNext(firstMid);
            firstMid = mid;
        }
        return firstMid;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + market.hashCode();
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
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
        final MarketId other = (MarketId) obj;
        if (!market.equals(other.market)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public final String getMarket() {
        return market;
    }

    public final long getId() {
        return id;
    }
}

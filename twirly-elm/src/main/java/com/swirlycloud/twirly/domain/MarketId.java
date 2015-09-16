/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.BasicJslNode;
import com.swirlycloud.twirly.util.Identifiable;

public final @NonNullByDefault class MarketId extends BasicJslNode implements Identifiable {

    private final long id;
    private String market;

    public MarketId(long id) {
        this.id = id;
        this.market = "";
    }

    public MarketId(long id, String market) {
        this.id = id;
        this.market = market;
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

    public final void setMarket(String market) {
        this.market = market;
    }

    @Override
    public final long getId() {
        return id;
    }

    public final String getMarket() {
        return market;
    }
}

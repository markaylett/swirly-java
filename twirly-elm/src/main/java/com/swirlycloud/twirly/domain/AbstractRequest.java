/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.util.NullUtil.nullIfEmpty;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.AbstractRbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.JsonUtil;

public abstract @NonNullByDefault class AbstractRequest extends AbstractRbNode implements Request {

    private static final long serialVersionUID = 1L;

    private transient @Nullable SlNode slNext;
    // Singly-linked node for RequestRefMap.
    private transient @Nullable Request refNext;

    /**
     * The executing trader.
     */
    protected final String trader;
    protected final String market;
    protected final String contr;
    protected final int settlDay;
    protected final long id;
    /**
     * Ref is optional.
     */
    protected final @Nullable String ref;
    protected final Side side;
    protected long lots;
    protected final long created;

    protected AbstractRequest(String trader, String market, String contr, int settlDay, long id,
            @Nullable String ref, Side side, long lots, long created) {
        this.trader = trader;
        this.market = market;
        this.contr = contr;
        this.settlDay = settlDay;
        this.id = id;
        this.ref = nullIfEmpty(ref);
        this.side = side;
        this.lots = lots;
        this.created = created;
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
        final AbstractRequest other = (AbstractRequest) obj;
        if (!market.equals(other.market)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void setSlNext(@Nullable SlNode next) {
        this.slNext = next;
    }

    @Override
    public final @Nullable SlNode slNext() {
        return slNext;
    }

    @Override
    public final void setRefNext(@Nullable Request next) {
        this.refNext = next;
    }

    @Override
    public final @Nullable Request refNext() {
        return refNext;
    }

    @Override
    public final String getTrader() {
        return trader;
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

    @Override
    public final long getId() {
        return id;
    }

    @Override
    public final @Nullable String getRef() {
        return ref;
    }

    @Override
    public final Side getSide() {
        return side;
    }

    @Override
    public final long getLots() {
        return lots;
    }

    @Override
    public final long getCreated() {
        return created;
    }
}

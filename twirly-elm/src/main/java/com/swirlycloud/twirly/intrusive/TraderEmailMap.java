/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import com.swirlycloud.twirly.rec.Trader;

public final class TraderEmailMap extends ObjectMap<String, Trader> {

    @Override
    protected final void setNext(Trader node, Trader next) {
        node.setSlNext(next);
    }

    @Override
    protected final Trader next(Trader node) {
        return (Trader) node.slNext();
    }
    
    @Override
    protected final int hashNode(Trader node) {
        return node.getEmail().hashCode();
    }

    @Override
    protected final boolean equalNode(Trader lhs, Trader rhs) {
        return lhs.getEmail().equals(rhs.getEmail());
    }

    @Override
    protected final boolean equalKey(Trader lhs, String rhs) {
        return lhs.getEmail().equals(rhs);
    }

    public TraderEmailMap(int capacity) {
        super(capacity);
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import com.swirlycloud.swirly.intrusive.AbstractObjectMap;

/**
 * Unordered TraderSess map keyed by email.
 */
public final class TraderSessMap extends AbstractObjectMap<String, TraderSess> {

    @Override
    protected final void setNext(TraderSess node, TraderSess next) {
        node.setSlNext(next);
    }

    @Override
    protected final TraderSess next(TraderSess node) {
        return (TraderSess) node.slNext();
    }

    @Override
    protected final int hashNode(TraderSess node) {
        return node.getEmail().hashCode();
    }

    @Override
    protected final boolean equalNode(TraderSess lhs, TraderSess rhs) {
        return lhs.getEmail().equals(rhs.getEmail());
    }

    @Override
    protected final boolean equalKey(TraderSess lhs, String rhs) {
        return lhs.getEmail().equals(rhs);
    }

    public TraderSessMap(int capacity) {
        super(capacity);
    }
}

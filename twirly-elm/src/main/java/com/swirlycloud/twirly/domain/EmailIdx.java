/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.intrusive.StringHashTable;

public final class EmailIdx extends StringHashTable<Trader> {

    @Override
    protected final void setNext(Trader node, Trader next) {
        node.emailNext = next;
    }

    @Override
    protected final Trader next(Trader node) {
        return node.emailNext;
    }

    @Override
    protected final int hashKey(Trader node) {
        return node.getEmail().hashCode();
    }

    @Override
    protected final boolean equalKey(Trader lhs, Trader rhs) {
        return lhs.getEmail().equals(rhs.getEmail());
    }

    @Override
    protected final boolean equalKeys(Trader lhs, String rhs) {
        return lhs.getEmail().equals(rhs);
    }

    public EmailIdx(int capacity) {
        super(capacity);
    }
}

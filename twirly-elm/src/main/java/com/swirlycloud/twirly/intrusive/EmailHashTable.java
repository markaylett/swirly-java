/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.node.SlNode;

public final class EmailHashTable extends BasicSlHashTable<String> {

    @Override
    protected final int hashKey(SlNode node) {
        return ((Trader) node).getEmail().hashCode();
    }

    @Override
    protected final boolean equalKey(SlNode lhs, SlNode rhs) {
        return ((Trader) lhs).getEmail().equals(((Trader) rhs).getEmail());
    }

    @Override
    protected final boolean equalKeyDirect(SlNode lhs, String rhs) {
        return ((Trader) lhs).getEmail().equals(rhs);
    }

    public EmailHashTable(int capacity) {
        super(capacity);
    }
}

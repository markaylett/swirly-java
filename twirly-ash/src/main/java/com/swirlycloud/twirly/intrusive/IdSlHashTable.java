/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.CollectionUtil.hashLong;

import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Identifiable;

public final class IdSlHashTable extends LongSlHashTable {

    private static long getId(SlNode node) {
        return ((Identifiable) node).getId();
    }

    @Override
    protected final int hashKey(SlNode node) {
        return hashLong(getId(node));
    }

    @Override
    protected final boolean equalKey(SlNode lhs, SlNode rhs) {
        return getId(lhs) == getId(rhs);
    }

    @Override
    protected final boolean equalKeyDirect(SlNode lhs, long rhs) {
        return getId(lhs) == rhs;
    }

    public IdSlHashTable(int capacity) {
        super(capacity);
    }
}

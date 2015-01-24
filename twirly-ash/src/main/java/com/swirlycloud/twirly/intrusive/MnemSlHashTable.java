/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Memorable;

public final class MnemSlHashTable extends BasicSlHashTable<String> {

    private static String getMnem(SlNode node) {
        return ((Memorable) node).getMnem();
    }

    @Override
    protected final int hashKey(SlNode node) {
        return getMnem(node).hashCode();
    }

    @Override
    protected final boolean equalKey(SlNode lhs, SlNode rhs) {
        return getMnem(lhs).equals(getMnem(rhs));
    }

    @Override
    protected final boolean equalKeyDirect(SlNode lhs, String rhs) {
        return getMnem(lhs).equals(rhs);
    }

    public MnemSlHashTable(int capacity) {
        super(capacity);
    }
}

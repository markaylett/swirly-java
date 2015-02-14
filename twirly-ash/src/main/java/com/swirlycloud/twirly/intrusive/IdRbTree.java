/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.CollectionUtil.compareLong;

import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Identifiable;

public final class IdRbTree extends LongRbTree {

    private static long getId(RbNode node) {
        return ((Identifiable) node).getId();
    }

    @Override
    protected final int compareKey(RbNode lhs, RbNode rhs) {
        return compareLong(getId(lhs), getId(rhs));
    }

    @Override
    protected final int compareKeyDirect(RbNode lhs, long rhs) {
        return compareLong(getId(lhs), rhs);
    }
}

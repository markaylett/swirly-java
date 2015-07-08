/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Memorable;

public final @NonNullByDefault class MnemRbTree extends BasicRbTree<String> {

    private static String getMnem(RbNode node) {
        return ((Memorable) node).getMnem();
    }

    @Override
    protected final int compareKey(RbNode lhs, RbNode rhs) {
        return getMnem(lhs).compareTo(getMnem(rhs));
    }

    @Override
    protected final int compareKeyDirect(RbNode lhs, String rhs) {
        return getMnem(lhs).compareTo(rhs);
    }
}

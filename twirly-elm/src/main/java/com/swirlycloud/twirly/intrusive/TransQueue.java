/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import com.swirlycloud.twirly.node.TransNode;

public final class TransQueue extends Queue<TransNode> {

    @Override
    protected final void setNext(TransNode node, TransNode next) {
        node.setTransNext(next);
    }

    @Override
    protected final TransNode next(TransNode node) {
        return node.transNext();
    }
}

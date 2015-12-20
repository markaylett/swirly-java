/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.intrusive;

import static com.swirlycloud.swirly.node.SlUtil.popNext;

import com.swirlycloud.swirly.node.SlNode;

public final class SlQueue extends AbstractQueue<SlNode> {

    /**
     * Clear the queue and set each next reference to null.
     */
    public final void clearAll() {
        SlNode node = getFirst();
        while (node != null) {
            node = popNext(node);
        }
        super.clear();
    }

    @Override
    protected final void setNext(SlNode node, SlNode next) {
        node.setSlNext(next);
    }

    @Override
    protected final SlNode next(SlNode node) {
        return node.slNext();
    }
}

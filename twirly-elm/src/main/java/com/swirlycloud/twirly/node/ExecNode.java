/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

public abstract class ExecNode extends BasicRbNode implements SlNode {

    private transient SlNode next;

    @Override
    public final void setSlNext(SlNode next) {
        this.next = next;
    }

    @Override
    public final SlNode slNext() {
        return next;
    }
}

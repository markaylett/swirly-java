/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class BasicSlNode implements SlNode {
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

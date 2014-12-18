/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.collection;

public abstract class BasicRbSlNode extends BasicRbNode implements SlNode {

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

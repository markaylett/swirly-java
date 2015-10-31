/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

import org.eclipse.jdt.annotation.Nullable;

public abstract class AbstractSlNode implements SlNode {
    private transient SlNode next;

    @Override
    public final void setSlNext(@Nullable SlNode next) {
        this.next = next;
    }

    @Override
    public final @Nullable SlNode slNext() {
        return next;
    }
}

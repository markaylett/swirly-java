/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

import org.eclipse.jdt.annotation.Nullable;

public abstract class AbstractJslNode implements JslNode {
    private transient JslNode next;

    @Override
    public final void setJslNext(@Nullable JslNode next) {
        this.next = next;
    }

    @Override
    public final @Nullable JslNode jslNext() {
        return next;
    }
}

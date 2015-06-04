/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

public final class SlUtil {
    private SlUtil() {
    }

    public static SlNode popNext(SlNode first) {
        final SlNode next = first.slNext();
        first.setSlNext(null);
        return next;
    }
}

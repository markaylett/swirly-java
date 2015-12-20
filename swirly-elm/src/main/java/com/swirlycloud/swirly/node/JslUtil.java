/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.node;

public final class JslUtil {
    private JslUtil() {
    }

    public static JslNode popNext(JslNode first) {
        final JslNode next = first.jslNext();
        first.setJslNext(null);
        return next;
    }
}

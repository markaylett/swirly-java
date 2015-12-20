/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.node;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public final class SlUtil {
    private SlUtil() {
    }

    public static SlNode popNext(@NonNull SlNode first) {
        final SlNode next = first.slNext();
        first.setSlNext(null);
        return next;
    }

    /**
     * Clear the list and set each next reference to null.
     */
    public static void nullify(@Nullable SlNode node) {
        while (node != null) {
            node = popNext(node);
        }
    }
}

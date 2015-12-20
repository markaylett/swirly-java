/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.intrusive;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SlQueueTest {
    @Test
    public final void test() {
        final SlQueue q = new SlQueue();
        assertTrue(q.isEmpty());
        assertNull(q.getFirst());
    }
}

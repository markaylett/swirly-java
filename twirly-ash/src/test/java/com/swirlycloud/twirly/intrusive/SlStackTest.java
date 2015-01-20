/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SlStackTest {
    @Test
    public final void test() {
        final SlStack s = new SlStack();
        assertTrue(s.isEmpty());
        assertNull(s.getFirst());
    }
}

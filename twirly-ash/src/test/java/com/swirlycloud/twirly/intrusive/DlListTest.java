/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DlListTest {
    @Test
    public final void test() {
        final DlList l = new DlList();
        assertTrue(l.isEmpty());
        assertTrue(l.getFirst().isEnd());
    }
}

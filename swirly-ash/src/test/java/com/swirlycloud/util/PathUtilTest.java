/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import static com.swirlycloud.util.StringUtil.splitPath;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PathUtilTest {
    @Test
    public final void testNull() {
        assertEquals(0, splitPath(null).length);
    }

    @Test
    public final void testEmpty() {
        assertEquals(0, splitPath("").length);
    }

    @Test
    public final void testSlash() {
        assertEquals(0, splitPath("/").length);
    }

    @Test
    public final void testOnePart() {
        final String[] parts = splitPath("foo");
        assertEquals(1, parts.length);
        assertEquals("foo", parts[0]);
    }

    @Test
    public final void testTwoParts() {
        final String[] parts = splitPath("foo/bar");
        assertEquals(2, parts.length);
        assertEquals("foo", parts[0]);
        assertEquals("bar", parts[1]);
    }

    @Test
    public final void testTrim() {
        final String[] parts = splitPath("/foo/bar/");
        assertEquals(2, parts.length);
        assertEquals("foo", parts[0]);
        assertEquals("bar", parts[1]);
    }
}

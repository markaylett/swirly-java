/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.doobry.web.WebUtil.alternateEmail;
import static org.doobry.web.WebUtil.splitPathInfo;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class WebUtilTest {
    @Test
    public final void testNull() {
        assertEquals(0, splitPathInfo(null).length);
    }

    @Test
    public final void testEmpty() {
        assertEquals(0, splitPathInfo("").length);
    }

    @Test
    public final void testSlash() {
        assertEquals(0, splitPathInfo("/").length);
    }

    @Test
    public final void testOnePart() {
        final String[] parts = splitPathInfo("foo");
        assertEquals(1, parts.length);
        assertEquals("foo", parts[0]);
    }

    @Test
    public final void testTwoParts() {
        final String[] parts = splitPathInfo("foo/bar");
        assertEquals(2, parts.length);
        assertEquals("foo", parts[0]);
        assertEquals("bar", parts[1]);
    }

    @Test
    public final void testTrim() {
        final String[] parts = splitPathInfo("/foo/bar/");
        assertEquals(2, parts.length);
        assertEquals("foo", parts[0]);
        assertEquals("bar", parts[1]);
    }

    @Test
    public final void testEmail() {
        assertEquals("emily.aylett@gmail.com", alternateEmail("emily.aylett@googlemail.com"));
    }
}

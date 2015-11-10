/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;

public final class TokenizerTest {
    @Test
    public final void testZero() {
        final Tokenizer toks = new Tokenizer("", ',');

        // End.
        assertFalse(toks.hasNext());
    }

    @Test
    public final void testOne() {
        final Tokenizer toks = new Tokenizer("101", ',');

        // First.
        assertTrue(toks.hasNext());
        String tok = toks.next();
        assertNotNull(tok);
        assertEquals("101", tok);

        // End.
        assertFalse(toks.hasNext());
    }

    @Test
    public final void testTwo() {
        final Tokenizer toks = new Tokenizer("101,202", ',');

        // First.
        assertTrue(toks.hasNext());
        String tok = toks.next();
        assertNotNull(tok);
        assertEquals("101", tok);

        // Second.
        assertTrue(toks.hasNext());
        tok = toks.next();
        assertNotNull(tok);
        assertEquals("202", tok);

        // End.
        assertFalse(toks.hasNext());
    }

    @Test
    public final void testThree() {
        final Tokenizer toks = new Tokenizer("101,202,303", ',');

        // First.
        assertTrue(toks.hasNext());
        String tok = toks.next();
        assertNotNull(tok);
        assertEquals("101", tok);

        // Second.
        assertTrue(toks.hasNext());
        tok = toks.next();
        assertNotNull(tok);
        assertEquals("202", tok);

        // Third.
        assertTrue(toks.hasNext());
        tok = toks.next();
        assertNotNull(tok);
        assertEquals("303", tok);

        // End.
        assertFalse(toks.hasNext());
    }

    @Test
    public final void testTrailing() {
        final Tokenizer toks = new Tokenizer("101,", ',');

        // First.
        assertTrue(toks.hasNext());
        String tok = toks.next();
        assertNotNull(tok);
        assertEquals("101", tok);

        // End.
        assertFalse(toks.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public final void testNoSuchElementException() {
        final Tokenizer toks = new Tokenizer("", ',');
        toks.next();
    }
}

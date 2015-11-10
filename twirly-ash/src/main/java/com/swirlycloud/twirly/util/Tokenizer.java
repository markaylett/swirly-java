/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;

public final @NonNullByDefault class Tokenizer implements Iterator<String> {
    private final String buf;
    private final char delim;
    private int pos;

    public Tokenizer(String buf, char delim) {
        this.buf = buf;
        this.delim = delim;
    }

    @Override
    public final boolean hasNext() {
        return pos < buf.length();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        for (int i = pos; i < buf.length(); ++i) {
            if (buf.charAt(i) == delim) {
                final String tok = buf.substring(pos, i);
                assert tok != null; 
                pos = i + 1;
                return tok;
            }
        }
        final String tok = buf.substring(pos);
        assert tok != null; 
        pos = buf.length();
        return tok;
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}

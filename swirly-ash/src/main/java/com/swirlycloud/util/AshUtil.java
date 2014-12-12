/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import java.io.IOException;

import com.swirlycloud.exception.UncheckedIOException;

public final class AshUtil {
    private AshUtil() {
    }

    public static Identifiable newId(final long id) {
        return new Identifiable() {
            @Override
            public final long getId() {
                return id;
            }
        };
    }

    public static final Identifiable ZERO_ID = newId(0);

    public static String toJson(Jsonifiable j, Object arg) {
        final StringBuilder sb = new StringBuilder();
        try {
            j.toJson(sb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }
}

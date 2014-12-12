/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import java.io.IOException;
import java.util.regex.Pattern;

import com.swirlycloud.exception.UncheckedIOException;

public final class StringUtil {
    private static final String[] EMPTY = {};
    private static final Pattern PATTERN = Pattern.compile("/");

    private StringUtil() {
    }

    public static String[] splitPath(String path) {
        if (path == null) {
            return EMPTY;
        }
        int begin = 0;
        int end = path.length();
        if (end > 0) {
            if (path.charAt(0) == '/') {
                ++begin;
            }
            if (end > 1 && path.charAt(end - 1) == '/') {
                --end;
            }
        }
        if (end - begin == 0) {
            return EMPTY;
        }
        return PATTERN.split(path.substring(begin, end));
    }

    public static String toJson(Jsonifiable j) {
        final StringBuilder sb = new StringBuilder();
        try {
            j.toJson(sb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }
}

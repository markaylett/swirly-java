/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import java.io.IOException;
import java.util.regex.Pattern;

import com.swirlycloud.twirly.exception.UncheckedIOException;

public final class StringUtil {
    private static final String[] EMPTY = {};
    private static final Pattern PATTERN = Pattern.compile("/");

    private StringUtil() {
    }

    public static final Params INTERNAL = new Params() {
        @SuppressWarnings("unchecked")
        @Override
        public final <T> T getParam(String name, Class<T> clazz) {
            return "internal".equals(name) ? (T) Boolean.TRUE : null;
        }
    };

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
            j.toJson(INTERNAL, sb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }
}

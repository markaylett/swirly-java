/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.util;

import java.util.regex.Pattern;

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
}

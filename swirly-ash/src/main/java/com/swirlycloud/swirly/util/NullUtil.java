/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.util;

public final class NullUtil {
    private NullUtil() {
    }

    public static Integer nullIfZero(int i) {
        return i != 0 ? Integer.valueOf(i) : null;
    }

    public static Long nullIfZero(long l) {
        return l != 0 ? Long.valueOf(l) : null;
    }

    public static String nullIfEmpty(String s) {
        return s != null && !s.isEmpty() ? s : null;
    }

    public static int zeroIfNull(Integer i) {
        return i != null ? i.intValue() : 0;
    }

    public static long zeroIfNull(Long l) {
        return l != null ? l.longValue() : 0;
    }

    public static String emptyIfNull(String s) {
        return s != null ? s : "";
    }
}

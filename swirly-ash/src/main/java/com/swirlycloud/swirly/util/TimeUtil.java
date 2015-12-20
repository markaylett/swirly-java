/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

public final @NonNullByDefault class TimeUtil {

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60L * SECOND;
    public static final long HOUR = 60L * MINUTE;
    public static final long DAY = 24L * HOUR;

    private TimeUtil() {
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}

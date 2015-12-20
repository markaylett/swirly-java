/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.date;

import org.eclipse.jdt.annotation.NonNull;

public enum WeekDay {
    SUN, MON, TUE, WED, THU, FRI, SAT;
    @SuppressWarnings("null")
    @NonNull
    public static WeekDay valueOfJd(int jd) {
        return WeekDay.values()[(jd + 1) % 7];
    }
}

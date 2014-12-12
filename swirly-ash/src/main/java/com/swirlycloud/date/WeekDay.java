/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.date;

public enum WeekDay {
    SUN, MON, TUE, WED, THU, FRI, SAT;
    public static WeekDay valueOfJd(int jd) {
        return WeekDay.values()[(jd + 1) % 7];
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.date;

/**
 * Gregorian date.
 */

public final class Ymd {
    /**
     * Year between 1801 and 2099 inclusive.
     */
    public int year;
    /**
     * Month between 1 and 12 inclusive.
     */
    public int mon;
    /**
     * Day of month between 1 and 31 inclusive.
     */
    public int mday;

    public Ymd(int year, int mon, int mday) {
        this.year = year;
        this.mon = mon;
        this.mday = mday;
    }
}

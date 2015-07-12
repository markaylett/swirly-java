/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.date;

public final class JulianDay {

    private JulianDay() {
    }

    /**
     * Gregorian to ISO8601 date.
     */
    public static int ymdToIso(int year, int mon, int mday) {
        assert mon <= 11;
        assert mday <= 31;
        return year * 10000 + (mon + 1) * 100 + mday;
    }

    /**
     * Gregorian date to Julian day.
     */
    public static int ymdToJd(int year, int mon, int mday) {
        // The formula given below was taken from the 1990 edition of the U.S. Naval Observatory's
        // Almanac for Computers.
        // See http://aa.usno.navy.mil/faq/docs/JD_Formula.php.

        final int i = year, j = mon + 1, k = mday;
        return k - 32075 + 1461 * (i + 4800 + (j - 14) / 12) / 4 + 367
                * (j - 2 - (j - 14) / 12 * 12) / 12 - 3 * ((i + 4900 + (j - 14) / 12) / 100) / 4;
    }

    /**
     * ISO8601 to Julian day.
     */
    public static int isoToJd(int iso) {
        final int year = iso / 10000;
        final int mon = (iso / 100 % 100) - 1;
        final int mday = iso % 100;
        return ymdToJd(year, mon, mday);
    }

    /**
     * Julian day to ISO8601.
     */
    public static int jdToIso(int jd) {
        // The formula given above was taken from the 1990 edition of the U.S. Naval Observatory's
        // Almanac for Computers.
        // See http://aa.usno.navy.mil/faq/docs/JD_Formula.php.

        int l = jd + 68569;
        final int n = 4 * l / 146097;
        l = l - (146097 * n + 3) / 4;
        int i = 4000 * (l + 1) / 1461001;
        l = l - 1461 * i / 4 + 31;
        int j = 80 * l / 2447;
        final int k = l - 2447 * j / 80;
        l = j / 11;
        j = j + 2 - 12 * l;
        i = 100 * (n - 49) + i + l;

        return i * 10000 + j * 100 + k;
    }

    /**
     * Juilian day to Modified Julian day. Epoch is November 17, 1858.
     */
    public static int jdToMjd(int jd) {
        return jd - 2400000;
    }

    /**
     * Modified Julian day to Julian day. Epoch is November 17, 1858.
     */
    public static int mjdToJd(int mjd) {
        return mjd + 2400000;
    }

    /**
     * Julian day to Truncated Julian day. Epoch is May 24, 1968.
     */
    public static int jdToTjd(int jd) {
        return jd - 2440000;
    }

    /**
     * Truncated Julian day to Gregorian date. Epoch is May 24, 1968.
     */
    public static int tjdToJd(int tjd) {
        return tjd + 2440000;
    }

    /**
     * Julian day to milliseconds since Unix epoch.
     */
    public static long jdToMillis(int jd) {
        // Julian day for January 1st, 1970.
        final int JD_UNIX_EPOC = 2440588;
        final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
        // Add half day for 12pm.
        return (jd - JD_UNIX_EPOC) * MILLIS_IN_DAY + (MILLIS_IN_DAY >> 1);
    }

    /**
     * Julian day to ISO8601 if argument is non-zero.
     */
    public static int maybeJdToIso(int jd) {
        return jd != 0 ? jdToIso(jd) : 0;
    }

    /**
     * ISO8601 to Julian day if argument is non-zero.
     */
    public static int maybeIsoToJd(int iso) {
        return iso != 0 ? isoToJd(iso) : 0;
    }
}

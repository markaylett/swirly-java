/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import com.swirlycloud.twirly.date.GregDate;

public abstract class DayCount {
    public abstract int diffDays(GregDate gd1, GregDate gd2);

    public final double diffYears(GregDate gd1, GregDate gd2) {
        return daysToYears(diffDays(gd1, gd2));
    }

    public abstract double daysToYears(int days);

    public static boolean isLastDayInFeb(int year, int mon, int mday) {
        // Zero-based month.
        return mon == 1 && GregDate.mdays(year, mon) <= mday;
    }

    public static final DayCount ACTUAL360 = new DayCount() {

        @Override
        public final int diffDays(GregDate gd1, GregDate gd2) {
            return gd1.diffDays(gd2);
        }

        @Override
        public final double daysToYears(int days) {
            return days / 360.0;
        }
    };

    public static final DayCount ACTUAL365FIXED = new DayCount() {

        @Override
        public final int diffDays(GregDate gd1, GregDate gd2) {
            return gd1.diffDays(gd2);
        }

        @Override
        public final double daysToYears(int days) {
            return days / 365.0;
        }
    };

    public static final DayCount US30360 = new DayCount() {

        @Override
        public final int diffDays(GregDate gd1, GregDate gd2) {
            final int y1 = gd1.getYear();
            final int m1 = gd1.getMon();
            int d1 = gd1.getMDay();
            final boolean f1 = isLastDayInFeb(y1, m1, d1);

            final int y2 = gd2.getYear();
            final int m2 = gd2.getMon();
            int d2 = gd2.getMDay();
            final boolean f2 = isLastDayInFeb(y2, m2, d2);

            // If the investment is EOM and (Date1 is the last day of February) and (Date2 is the
            // last day of February), then change D2 to 30.

            d2 = f1 && f2 ? 30 : d2;

            // If the investment is EOM and (Date1 is the last day of February), then change D1 to
            // 30.

            d1 = f1 ? 30 : d1;

            // If D2 is 31 and D1 is 30 or 31, then change D2 to 30.

            d2 = d2 == 31 && d1 >= 30 ? 30 : d2;

            // Rule 4.
            // If D1 is 31, then change D1 to 30.

            d1 = d1 == 31 ? 30 : d1;

            return 360 * (y2 - y1) + 30 * (m2 - m1) + (d2 - d1);
        }

        @Override
        public final double daysToYears(int days) {
            return days / 360.0;
        }
    };

    public static final DayCount EU30360 = new DayCount() {

        @Override
        public final int diffDays(GregDate gd1, GregDate gd2) {
            final int y1 = gd1.getYear();
            final int m1 = gd1.getMon();
            int d1 = gd1.getMDay();

            final int y2 = gd2.getYear();
            final int m2 = gd2.getMon();
            int d2 = gd2.getMDay();

            // If D2 is 31, then change D2 to 30.
            d2 = d2 == 31 ? 30 : d2;

            // If D1 is 31, then change D1 to 30.
            d1 = d1 == 31 ? 30 : d1;

            return 360 * (y2 - y1) + 30 * (m2 - m1) + (d2 - d1);
        }

        @Override
        public final double daysToYears(int days) {
            return days / 360.0;
        }
    };
}

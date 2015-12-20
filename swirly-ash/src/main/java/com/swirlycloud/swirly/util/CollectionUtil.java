/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.util;

public final class CollectionUtil {
    private CollectionUtil() {

    }

    public static int compareInt(int lhs, int rhs) {
        int i;
        if (lhs < rhs) {
            i = -1;
        } else if (lhs > rhs) {
            i = 1;
        } else {
            i = 0;
        }
        return i;
    }

    public static int compareLong(long lhs, long rhs) {
        int i;
        if (lhs < rhs) {
            i = -1;
        } else if (lhs > rhs) {
            i = 1;
        } else {
            i = 0;
        }
        return i;
    }

    public static int hashLong(long id) {
        return (int) (id ^ id >>> 32);
    }

    public static int midpoint(int lo, int hi) {
        return lo + (hi - lo) / 2;
    }

    public static int binsearch(double[] xs, double x) {
        int lo = 0, hi = xs.length - 1;
        while (hi - lo > 1) {
            final int mid = midpoint(lo, hi);
            if (xs[mid] <= x) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return lo;
    }
}

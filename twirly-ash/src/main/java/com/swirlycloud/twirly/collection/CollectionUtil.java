/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

public final class CollectionUtil {
    private CollectionUtil() {
    }

    public static int midpoint(int lo, int hi) {
        return lo + (hi - lo) / 2;
    }

    public static int binsearch(double[] xs, double x) {
        int lo = 0, hi = xs.length - 1;
        while (lo < hi) {
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

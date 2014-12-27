/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

public final class YieldUtil {
    private YieldUtil() {
    }

    public static double periodToCont(int n, double r) {
        return n * Math.log(1 + r / n);
    }

    public static double contToPeriod(int n, double r) {
        return n * (Math.exp(r / n) - 1);
    }

    public static double annualToPeriod(int n, double r) {
        return n * (Math.pow(1 + r, 1 / n) - 1);
    }

    public static double annualToTime(double fv, double r) {
        return Math.log(fv) / Math.log(1 + r);
    }
}

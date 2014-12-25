/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

public final class InterestUtil {
    private InterestUtil() {
    }

    public final double periodToCont(int n, double r) {
        return n * Math.log(1 + r / n);
    }

    public final double contToPeriod(int n, double r) {
        return n * (Math.exp(r / n) - 1);
    }

    public final double annualToPeriod(int n, double r) {
        return n * (Math.pow(1 + r, 1 / n) - 1);
    }

    public final double annualToTime(double fv, double r) {
        return Math.log(fv) / Math.log(1 + r);
    }
}

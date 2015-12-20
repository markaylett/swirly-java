/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.math;

import static com.swirlycloud.swirly.util.CollectionUtil.binsearch;

public final class MathUtil {

    private MathUtil() {
    }

    public static double roundHalfAway(double d) {
        if (d < 0) {
            d = Math.ceil(d - 0.5);
        } else if (d > 0) {
            d = Math.floor(d + 0.5);
        }
        return d;
    }

    /**
     * Round-up to the next power of two.
     * 
     * @param n
     *            The number to round-up.
     * @return the next power of two.
     */
    public static int nextPow2(int n) {
        int r = Integer.highestOneBit(n);
        if (r != 0) {
            if (Integer.bitCount(n) > 1) {
                r <<= 1;
            }
        } else {
            r = 1;
        }
        return r;
    }

    public static double[] derivative1(double[] x, double[] y) {
        final int n = x.length - 1;
        final double[] y2 = new double[x.length];
        y2[0] = (y[1] - y[0]) / (x[1] - x[0]);
        y2[n] = (y[n] - y[n - 1]) / (x[n] - x[n - 1]);
        for (int i = 1; i < n; ++i) {
            y2[i] = (y[i + 1] - y[i - 1]) / (x[i + 1] - x[i - 1]);
        }
        return y2;
    }

    public static double[] derivative2(double[] x, double[] y, Number yp1, Number ypn) {
        final int n = x.length - 1;
        final double[] y2 = new double[x.length];
        final double[] u = new double[x.length];
        if (null == yp1) {
            y2[0] = 0.0;
            u[0] = 0.0;
        } else {
            y2[0] = -0.5;
            u[0] = 3 / (x[1] - x[0]) * ((y[1] - y[0]) / (x[1] - x[0]) - yp1.doubleValue());
        }
        for (int i = 1; i < n; ++i) {
            final double sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
            final double p = sig * y2[i - 1] + 2.0;
            y2[i] = (sig - 1.0) / p;
            u[i] = (6.0
                    * ((y[i + 1] - y[i]) / (x[i + 1] - x[i])
                            - (y[i] - y[i - 1]) / (x[i] - x[i - 1]))
                    / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
        }
        double qn, un;
        if (null == ypn) {
            qn = 0.0;
            un = 0.0;
        } else {
            qn = 0.5;
            un = 3.0 / (x[n] - x[n - 1])
                    * (ypn.doubleValue() - (y[n] - y[n - 1]) / (x[n] - x[n - 1]));
        }
        y2[n] = (un - qn * u[n - 1]) / (qn * y2[n - 1] + 1.0);
        for (int i = n - 1; 0 <= i; --i) {
            y2[i] = y2[i] * y2[i + 1] + u[i];
        }
        return y2;
    }

    public static double linearInterp(double[] xs, double[] ys, double x) {
        final int i = binsearch(xs, x);
        final int j = i + 1;
        final double x1 = xs[i];
        final double y1 = ys[i];
        final double x2 = i == xs.length - 1 ? x : xs[j];
        final double y2 = i == xs.length - 1 ? ys[i] : ys[j];
        final double xd = x2 - x1;
        final double yd = y2 - y1;
        if (xd == 0.0) {
            throw new IllegalArgumentException("zero interval in curve data");
        }
        return y1 + (x - x1) * (yd / xd);
    }

    public static double splineInterp(double[] xs, double[] ys, double[] y2, double x) {
        if (y2 == null) {
            return ys[0];
        }
        final int i = binsearch(xs, x);
        final int lo = i, hi = i + 1;
        final double h = xs[hi] - xs[lo];
        if (h == 0.0) {
            throw new IllegalArgumentException("zero interval in curve data");
        }
        final double a = (xs[hi] - x) / h;
        final double b = (x - xs[lo]) / h;
        return a * ys[lo] + b * ys[hi]
                + ((a * a * a - a) * y2[lo] + (b * b * b - b) * y2[hi]) * h * h / 6.0;
    }

}

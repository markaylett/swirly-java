/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.math;

import static com.swirlycloud.twirly.math.MathUtil.splineInterp;

public final class SplineInterp implements Interp {
    private final double[] xs, ys, y2;

    public SplineInterp(double[] xs, double[] ys) {
        this.xs = xs;
        this.ys = ys;
        y2 = xs.length > 1 ? MathUtil.derivative2(xs, ys, null, null) : null;
    }

    @Override
    public final double interp(double x) {
        return splineInterp(xs, ys, y2, x);
    }
}

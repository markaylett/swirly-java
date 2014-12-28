/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.math;

import static com.swirlycloud.twirly.math.MathUtil.linearInterp;

public final class LinearInterp implements Interp {
    private final double[] xs, ys;

    public LinearInterp(double[] xs, double[] ys) {
        this.xs = xs;
        this.ys = ys;
    }

    @Override
    public final double interp(double x) {
        return linearInterp(xs, ys, x);
    }
}

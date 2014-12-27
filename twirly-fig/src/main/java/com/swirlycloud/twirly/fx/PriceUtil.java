/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

public final class PriceUtil {
    private PriceUtil() {
    }

    public static double fwdPrice(double spot, double fv1, double fv2) {
        return spot * fv2 / fv1;
    }
}

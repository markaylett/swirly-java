/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.math.MathUtil;

public final class Conv {
    private Conv() {
    }

    public static double fractToReal(int numer, int denom) {
        return (double) numer / (double) denom;
    }

    public static long realToIncs(double real, double incSize) {
        return (long) MathUtil.roundHalfAway(real / incSize);
    }

    public static double incsToReal(long incs, double incSize) {
        return incs * incSize;
    }

    /**
     * Convert quantity to lots.
     */

    public static long qtyToLots(double qty, double qtyInc) {
        return realToIncs(qty, qtyInc);
    }

    /**
     * Convert lots to quantity.
     */

    public static double lotsToQty(long lots, double qtyInc) {
        return incsToReal(lots, qtyInc);
    }

    /**
     * Convert price to ticks.
     */

    public static long priceToTicks(double price, double priceInc) {
        return realToIncs(price, priceInc);
    }

    /**
     * Convert ticks to price.
     */

    public static double ticksToPrice(long ticks, double priceInc) {
        return incsToReal(ticks, priceInc);
    }

    /**
     * Number of decimal places in real.
     */

    public static int realToDp(double d) {
        int dp;
        for (dp = 0; dp < 9; ++dp) {
            final double fp = d % 1.0;
            if (fp < 0.000000001) {
                break;
            }
            d *= 10;
        }
        return dp;
    }

    /**
     * Decimal places as real.
     */

    public static double dpToReal(int dp) {
        return Math.pow(10, -dp);
    }
}

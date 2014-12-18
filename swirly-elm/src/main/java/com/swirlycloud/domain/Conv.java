/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

public final class Conv {
    private Conv() {
    }

    public static double fractToReal(int numer, int denom) {
        return (double) numer / (double) denom;
    }

    public static long realToIncs(double real, double inc_size) {
        return Math.round(real / inc_size);
    }

    public static double incsToReal(long incs, double inc_size) {
        return incs * inc_size;
    }

    /**
     * Convert quantity to lots.
     */

    public static long qtyToLots(double qty, double qty_inc) {
        return realToIncs(qty, qty_inc);
    }

    /**
     * Convert lots to quantity.
     */

    public static double lotsToQty(long lots, double qty_inc) {
        return incsToReal(lots, qty_inc);
    }

    /**
     * Convert price to ticks.
     */

    public static long priceToTicks(double price, double price_inc) {
        return realToIncs(price, price_inc);
    }

    /**
     * Convert ticks to price.
     */

    public static double ticksToPrice(long ticks, double price_inc) {
        return incsToReal(ticks, price_inc);
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

public abstract class Interest {
    /**
     * Calculate future-value multiplier.
     * 
     * @param t
     *            The time.
     * @param r
     *            The rate.
     * @return future-value multiplier.
     */
    public abstract double fv(double t, double r);

    /**
     * Calculate present-value multiplier.
     * 
     * This function is the inverse of {@link #fv()} .
     * 
     * @param t
     *            The time.
     * @param r
     *            The rate.
     * @return present-value multiplier.
     */
    public double pv(double t, double r) {
        return 1 / fv(t, r);
    }

    /**
     * Calculate interest-rate from future-value.
     * 
     * @param t
     *            The time.
     * @param fv
     *            Future value.
     * @return the interest-rate.
     */
    public abstract double ir(double t, double fv);

    public static final Interest SIMPLE_INTEREST = new Interest() {

        @Override
        public final double fv(double t, double r) {
            return 1 + r * t;
        }

        @Override
        public final double ir(double t, double fv) {
            return (fv - 1) / t;
        }
    };

    public static final Interest ANNUAL_COMP = new Interest() {

        @Override
        public final double fv(double t, double r) {
            return Math.pow(1 + r, t);
        }

        @Override
        public final double ir(double t, double fv) {
            return Math.pow(fv, 1 / t) - 1;
        }
    };

    public static final Interest CONT_COMP = new Interest() {

        @Override
        public final double fv(double t, double r) {
            return Math.exp(r * t);
        }

        @Override
        public double pv(double t, double r) {
            return Math.exp(-r * t);
        }

        @Override
        public final double ir(double t, double fv) {
            return Math.log(fv) / t;
        }
    };

    public static Interest newPeriodComp(final int n) {
        return new Interest() {

            @Override
            public final double fv(double t, double r) {
                return Math.pow(1 + r / n, t * n);
            }

            @Override
            public final double ir(double t, double fv) {
                return n * (Math.pow(fv, (1 / (t * n))) - 1);
            }
        };
    }
}

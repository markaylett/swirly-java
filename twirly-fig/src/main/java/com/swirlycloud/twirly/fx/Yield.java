/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

public abstract class Yield {
    /**
     * Calculate future-value factor.
     * 
     * @param r
     *            The rate.
     * @param t
     *            The time.
     * 
     * @return future-value factor.
     */
    public abstract double fv(double r, double t);

    /**
     * Calculate present-value (or discount) factor.
     * 
     * This function is the inverse of {@link #fv()} .
     * 
     * @param r
     *            The rate.
     * @param t
     *            The time.
     * 
     * @return present-value factor.
     */
    public double pv(double r, double t) {
        return 1 / fv(r, t);
    }

    /**
     * Calculate interest-rate from future-value.
     * 
     * @param fv
     *            Future value.
     * @param t
     *            The time.
     * 
     * @return the interest-rate.
     */
    public abstract double ir(double fv, double t);

    /**
     * Forward rate.
     * 
     * @param r1
     *            First rate.
     * @param t1
     *            First time.
     * @param r2
     *            Second rate.
     * @param t2
     *            Second time.
     * @return the implied forward-rate.
     * @see {@link http://en.wikipedia.org/wiki/Forward_rate}
     */
    public double fr(double r1, double t1, double r2, double t2) {
        return ir(fv(r2, t2) / fv(r1, t1), t2 - t1);
    }

    public static final Yield SIMPLE_INTEREST = new Yield() {

        @Override
        public final double fv(double r, double t) {
            return 1 + r * t;
        }

        @Override
        public final double ir(double fv, double t) {
            return (fv - 1) / t;
        }
    };

    public static final Yield ANNUAL_COMP = new Yield() {

        @Override
        public final double fv(double r, double t) {
            return Math.pow(1 + r, t);
        }

        @Override
        public final double ir(double fv, double t) {
            return Math.pow(fv, 1 / t) - 1;
        }
    };

    public static final Yield CONT_COMP = new Yield() {

        @Override
        public final double fv(double r, double t) {
            return Math.exp(r * t);
        }

        @Override
        public double pv(double r, double t) {
            return Math.exp(-r * t);
        }

        @Override
        public final double ir(double fv, double t) {
            return Math.log(fv) / t;
        }

        @Override
        public double fr(double r1, double t1, double r2, double t2) {
            return (r2 * t2 - r1 * t1) / (t2 - t1);
        }
    };

    public static Yield newPeriodComp(final int n) {
        return new Yield() {

            @Override
            public final double fv(double r, double t) {
                return Math.pow(1 + r / n, t * n);
            }

            @Override
            public final double ir(double fv, double t) {
                return n * (Math.pow(fv, (1 / (t * n))) - 1);
            }
        };
    }
}

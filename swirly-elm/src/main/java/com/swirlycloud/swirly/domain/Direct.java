/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.domain;

import com.swirlycloud.swirly.util.Invertible;

public enum Direct implements Invertible<Direct> {
    /**
     * Aggressor buys. I.e. taker lifts the offer.
     */
    PAID(1),
    /**
     * Aggressor sells. I.e. taker hits the bid.
     */
    GIVEN(-1);
    private final int id;

    private Direct(int id) {
        this.id = id;
    }

    /**
     * @param id
     *            Numeric identifier.
     * @return direct or null if {@code id} is zero.
     * @throws IllegalArgumentException
     *             if {@code id} is invalid.
     */
    public static Direct valueOf(int id) {
        Direct val;
        switch (id) {
        case 0:
            val = null;
            break;
        case 1:
            val = Direct.PAID;
            break;
        case -1:
            val = Direct.GIVEN;
            break;
        default:
            throw new IllegalArgumentException("invalid direction");
        }
        return val;
    }

    @Override
    public final Direct inverse() {
        return this == Direct.PAID ? Direct.GIVEN : Direct.PAID;
    }

    public final int intValue() {
        return this.id;
    }
}

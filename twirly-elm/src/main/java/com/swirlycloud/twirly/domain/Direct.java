/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public enum Direct {
    /**
     * Aggressor buys.
     */
    PAID(1),
    /**
     * Aggressor sells.
     */
    GIVEN(-1);
    private final int id;

    private Direct(int id) {
        this.id = id;
    }

    public static Direct valueOf(int id) {
        Direct val;
        switch (id) {
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

    public final int intValue() {
        return this.id;
    }
}

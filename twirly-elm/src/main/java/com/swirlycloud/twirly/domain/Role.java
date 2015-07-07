/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.util.Invertible;

public enum Role implements Invertible<Role> {
    /**
     * Passive buyer or seller that receives the spread.
     */
    MAKER(1), /**
     * Aggressive buyer or seller that crosses the market and pays the spread.
     */
    TAKER(2);
    private final int id;

    private Role(int id) {
        this.id = id;
    }

    /**
     * @param id
     *            Numeric identifier.
     * @return role or null if {@code id} is zero.
     * @throws IllegalArgumentException
     *             if {@code id} is invalid.
     */
    public static Role valueOf(int id) {
        Role val;
        switch (id) {
        case 0:
            val = null;
        case 1:
            val = Role.MAKER;
            break;
        case 2:
            val = Role.TAKER;
            break;
        default:
            throw new IllegalArgumentException("invalid role");
        }
        return val;
    }

    @Override
    public final Role inverse() {
        return this == Role.MAKER ? Role.TAKER : Role.MAKER;
    }

    public final int intValue() {
        return this.id;
    }
}

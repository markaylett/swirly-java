/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public enum Role {
    MAKER(1), TAKER(2);
    private final int id;

    private Role(int id) {
        this.id = id;
    }

    public static Role valueOf(int id) {
        Role val;
        switch (id) {
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

    public final int intValue() {
        return this.id;
    }
}

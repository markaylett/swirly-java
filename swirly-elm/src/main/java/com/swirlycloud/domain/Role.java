/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

public enum Role {
    MAKER(1), TAKER(2);
    private final int value;

    private Role(int value) {
        this.value = value;
    }

    public final int intValue() {
        return this.value;
    }
}

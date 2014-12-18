/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

public enum Direct {
    /**
     * Aggressor buys.
     */
    PAID(1),
    /**
     * Aggressor sells.
     */
    GIVEN(-1);
    private final int value;

    private Direct(int value) {
        this.value = value;
    }

    public final int intValue() {
        return this.value;
    }
}

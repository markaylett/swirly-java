/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public enum Action {
    BUY(1), SELL(-1);
    private final int value;

    private Action(int value) {
        this.value = value;
    }

    public final int intValue() {
        return this.value;
    }
}

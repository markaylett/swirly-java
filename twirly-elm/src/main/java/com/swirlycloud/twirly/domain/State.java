/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public enum State {
    NEW(1), REVISE(2), CANCEL(3), TRADE(4);
    private final int value;

    private State(int value) {
        this.value = value;
    }

    public final int intValue() {
        return this.value;
    }
}

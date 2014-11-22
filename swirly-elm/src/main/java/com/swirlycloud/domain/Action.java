/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

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

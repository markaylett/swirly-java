/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public enum Action {
    BUY(1), SELL(-1);
    private final int id;

    private Action(int id) {
        this.id = id;
    }

    public static Action valueOf(int id) {
        Action val;
        switch (id) {
        case 1:
            val = Action.BUY;
            break;
        case -1:
            val = Action.SELL;
            break;
        default:
            throw new IllegalArgumentException("invalid action");
        }
        return val;
    }

    public final int intValue() {
        return this.id;
    }
}

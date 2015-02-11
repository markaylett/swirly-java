/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public enum State {
    NEW(1), REVISE(2), CANCEL(3), TRADE(4);
    private final int id;

    private State(int id) {
        this.id = id;
    }

    public static State valueOf(int id) {
        State val;
        switch (id) {
        case 1:
            val = State.NEW;
            break;
        case 2:
            val = State.REVISE;
            break;
        case 3:
            val = State.CANCEL;
            break;
        case 4:
            val = State.TRADE;
            break;
        default:
            throw new IllegalArgumentException("invalid state");
        }
        return val;
    }

    public final int intValue() {
        return this.id;
    }
}

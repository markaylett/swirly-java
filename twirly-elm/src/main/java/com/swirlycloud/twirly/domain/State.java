/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault enum State {
    NONE(0),
    /**
     * Initial state of a resting order placed in the order-book.
     */
    NEW(1),
    /**
     * State of a resting order that has been revised.
     */
    REVISE(2),
    /**
     * State of a resting order that has been cancelled.
     */
    CANCEL(3),
    /**
     * State of an order that has been partially or fully filled.
     */
    TRADE(4),
    /**
     * State of a resting order that is pending cancel.
     */
    PECAN(5);
    private final int id;

    private State(int id) {
        this.id = id;
    }

    /**
     * @param id
     *            Numeric identifier.
     * @return state or null if {@code id} is zero.
     * @throws IllegalArgumentException
     *             if {@code id} is invalid.
     */
    public static State valueOf(int id) {
        State val;
        switch (id) {
        case 0:
            val = State.NONE;
            break;
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
        case 5:
            val = State.PECAN;
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

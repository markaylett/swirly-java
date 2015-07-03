/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.util.Invertible;

public @NonNullByDefault enum Action implements Invertible<Action> {
    BUY(1), SELL(-1);
    private final int id;

    private Action(int id) {
        this.id = id;
    }

    /**
     * @param id
     *            Numeric identifier.
     * @return action or null if {@code id} is zero.
     * @throws IllegalArgumentException
     *             if {@code id} is invalid.
     */
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

    @Override
    public final Action inverse() {
        return this == Action.BUY ? Action.SELL : Action.BUY;
    }

    public final int intValue() {
        return this.id;
    }
}

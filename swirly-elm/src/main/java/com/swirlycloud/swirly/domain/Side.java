/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.swirly.util.Invertible;

public @NonNullByDefault enum Side implements Invertible<Side> {
    BUY(1), SELL(-1);
    private final int id;

    private Side(int id) {
        this.id = id;
    }

    /**
     * @param id
     *            Numeric identifier.
     * @return side or null if {@code id} is zero.
     * @throws IllegalArgumentException
     *             if {@code id} is invalid.
     */
    public static Side valueOf(int id) {
        Side val;
        switch (id) {
        case 1:
            val = Side.BUY;
            break;
        case -1:
            val = Side.SELL;
            break;
        default:
            throw new IllegalArgumentException("invalid side");
        }
        return val;
    }

    @Override
    public final Side inverse() {
        return this == Side.BUY ? Side.SELL : Side.BUY;
    }

    public final int intValue() {
        return this.id;
    }
}

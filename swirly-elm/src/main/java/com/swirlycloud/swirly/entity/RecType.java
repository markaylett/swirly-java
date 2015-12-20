/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

public enum RecType {
    /**
     * Asset.
     */
    ASSET(1),
    /**
     * Contract.
     */
    CONTR(2),
    /**
     * Market.
     */
    MARKET(3),
    /**
     * Trader.
     */
    TRADER(4);
    private final int id;

    private RecType(int id) {
        this.id = id;
    }

    /**
     * @param id
     *            Numeric identifier.
     * @return rec-type or null if {@code id} is zero.
     * @throws IllegalArgumentException
     *             if {@code id} is invalid.
     */
    public static RecType valueOf(int id) {
        RecType val;
        switch (id) {
        case 0:
            val = null;
            break;
        case 1:
            val = RecType.ASSET;
            break;
        case 2:
            val = RecType.CONTR;
            break;
        case 3:
            val = RecType.MARKET;
            break;
        case 4:
            val = RecType.TRADER;
            break;
        default:
            throw new IllegalArgumentException("invalid rec-type");
        }
        return val;
    }

    public final int intValue() {
        return this.id;
    }
}

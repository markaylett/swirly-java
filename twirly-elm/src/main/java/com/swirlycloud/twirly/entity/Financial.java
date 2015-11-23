/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Fields common to both Order and Exec.
 * 
 * @author Mark Aylett
 */
public @NonNullByDefault interface Financial extends Entity {

    String getMarket();

    String getContr();

    /**
     * @return settlement-day or zero if there is no settlement date.
     */
    int getSettlDay();

    /**
     * @return true if settlement-day is non-zero.
     */
    boolean isSettlDaySet();
}

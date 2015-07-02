/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Fields common to both Order and Exec.
 */

public @NonNullByDefault interface Financial {

    String getMarket();

    String getContr();

    /**
     * @return settlement-date or zero if there is no settlement date.
     */
    int getSettlDay();
}

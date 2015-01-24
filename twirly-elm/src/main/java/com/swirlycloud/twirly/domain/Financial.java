/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

/**
 * Fields common to both Order and Exec.
 */

public interface Financial {

    String getMarket();

    String getContr();

    int getSettlDay();
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.BusinessRejectReason;
import com.swirlycloud.swirly.fix.OrderRejectReason;

public class MarketNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public MarketNotFoundException(String msg) {
        super(msg);
    }

    public MarketNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getBusinessRejectReason() {
        return BusinessRejectReason.UNKNOWN_SECURITY;
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.UNKNOWN_SYMBOL;
    }
}

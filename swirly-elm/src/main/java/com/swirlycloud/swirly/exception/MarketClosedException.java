/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.BusinessRejectReason;
import com.swirlycloud.swirly.fix.CancelRejectReason;
import com.swirlycloud.swirly.fix.OrderRejectReason;

public class MarketClosedException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public MarketClosedException(String msg) {
        super(msg);
    }

    public MarketClosedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getBusinessRejectReason() {
        return BusinessRejectReason.OTHER;
    }

    @Override
    public int getCancelRejectReason() {
        return CancelRejectReason.OTHER;
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.EXCHANGE_CLOSED;
    }
}

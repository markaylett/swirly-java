/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.CancelRejectReason;
import com.swirlycloud.twirly.fix.OrderRejectReason;

public class OrderNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(String msg) {
        super(msg);
    }

    public OrderNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getCancelRejectReason() {
        return CancelRejectReason.UNKNOWN_ORDER;
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.UNKNOWN_ORDER;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.OrderRejectReason;

public class InvalidTicksException extends InvalidException {

    private static final long serialVersionUID = 1L;

    public InvalidTicksException(String msg) {
        super(msg);
    }

    public InvalidTicksException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.ORDER_EXCEEDS_LIMIT;
    }
}

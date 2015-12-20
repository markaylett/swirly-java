/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.OrderRejectReason;

public class InvalidLotsException extends InvalidException {

    private static final long serialVersionUID = 1L;

    public InvalidLotsException(String msg) {
        super(msg);
    }

    public InvalidLotsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.INCORRECT_QUANTITY;
    }
}

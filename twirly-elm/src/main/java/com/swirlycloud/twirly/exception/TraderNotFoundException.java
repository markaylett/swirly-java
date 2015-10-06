/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.OrderRejectReason;

public class TraderNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public TraderNotFoundException(String msg) {
        super(msg);
    }

    public TraderNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.UNKNOWN_ACCOUNT;
    }
}

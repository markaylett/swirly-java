/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.CancelRejectReason;
import com.swirlycloud.swirly.fix.OrderRejectReason;

public class TooLateException extends BadRequestException {

    private static final long serialVersionUID = 1L;

    public TooLateException(String msg) {
        super(msg);
    }

    public TooLateException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getCancelRejectReason() {
        return CancelRejectReason.TOO_LATE_TO_CANCEL;
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.TOO_LATE_TO_ENTER;
    }
}

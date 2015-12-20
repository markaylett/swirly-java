/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.CancelRejectReason;
import com.swirlycloud.swirly.fix.OrderRejectReason;

public final class RefAlreadyExistsException extends AlreadyExistsException {

    private static final long serialVersionUID = 1L;

    public RefAlreadyExistsException(String msg) {
        super(msg);
    }

    public RefAlreadyExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getCancelRejectReason() {
        return CancelRejectReason.DUPLICATE_CLORDID_RECEIVED;
    }

    @Override
    public int getOrderRejectReason() {
        return OrderRejectReason.DUPLICATE_ORDER;
    }
}

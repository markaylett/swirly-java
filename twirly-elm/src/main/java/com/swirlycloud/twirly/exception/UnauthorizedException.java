/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.BusinessRejectReason;

/**
 * Similar to 403 Forbidden, but specifically for use when authentication is required and has failed
 * or has not yet been provided.
 * 
 * @author Mark Aylett
 */
public final class UnauthorizedException extends ServException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String msg) {
        super(msg);
    }

    public UnauthorizedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getHttpStatus() {
        return 403;
    }

    @Override
    public int getBusinessRejectReason() {
        return BusinessRejectReason.NOT_AUTHORIZED;
    }
}

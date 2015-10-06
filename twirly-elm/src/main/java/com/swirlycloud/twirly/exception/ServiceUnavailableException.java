/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.BusinessRejectReason;

/**
 * The server is currently unavailable (because it is overloaded or down for maintenance).
 * Generally, this is a temporary state.
 * 
 * @author Mark Aylett
 */
public final class ServiceUnavailableException extends ServException {

    private static final long serialVersionUID = 1L;

    public ServiceUnavailableException(String msg) {
        super(msg);
    }

    public ServiceUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getHttpStatus() {
        return 503;
    }

    @Override
    public int getBusinessRejectReason() {
        return BusinessRejectReason.APPLICATION_NOT_AVAILABLE;
    }
}

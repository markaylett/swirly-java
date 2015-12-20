/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.BusinessRejectReason;

/**
 * The server has not found anything matching the Request-URI. No indication is given of whether the
 * condition is temporary or permanent. The 410 (Gone) status code SHOULD be used if the server
 * knows, through some internally configurable mechanism, that an old resource is permanently
 * unavailable and has no forwarding address. This status code is commonly used when the server does
 * not wish to reveal exactly why the request has been refused, or when no other response is
 * applicable.
 * 
 * @author Mark Aylett
 */
public class NotFoundException extends ServException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String msg) {
        super(msg);
    }

    public NotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getHttpStatus() {
        return 404;
    }

    @Override
    public int getBusinessRejectReason() {
        return BusinessRejectReason.UNKNOWN_ID;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.BusinessRejectReason;

/**
 * The server is currently unable to handle the request due to a temporary overloading or
 * maintenance of the server. The implication is that this is a temporary condition which will be
 * alleviated after some delay. If known, the length of the delay MAY be indicated in a Retry-After
 * header. If no Retry-After is given, the client SHOULD handle the response as it would for a 500
 * response.
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

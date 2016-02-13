/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.BusinessRejectReason;

/**
 * The request requires user authentication. The response MUST include a WWW-Authenticate header
 * field (section 14.47) containing a challenge applicable to the requested resource. The client MAY
 * repeat the request with a suitable Authorization header field (section 14.8). If the request
 * already included Authorization credentials, then the 401 response indicates that authorization
 * has been refused for those credentials. If the 401 response contains the same challenge as the
 * prior response, and the user agent has already attempted authentication at least once, then the
 * user SHOULD be presented the entity that was given in the response, since that entity might
 * include relevant diagnostic information. HTTP access authentication is explained in
 * "HTTP Authentication: Basic and Digest Access Authentication".
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
        return 401;
    }

    @Override
    public int getBusinessRejectReason() {
        return BusinessRejectReason.NOT_AUTHORIZED;
    }
}

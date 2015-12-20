/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import com.swirlycloud.swirly.fix.BusinessRejectReason;

/**
 * The method specified in the Request-Line is not allowed for the resource identified by the
 * Request-URI. The response MUST include an Allow header containing a list of valid methods for the
 * requested resource.
 * 
 * @author Mark Aylett
 */
public final class MethodNotAllowedException extends ServException {

    private static final long serialVersionUID = 1L;

    public MethodNotAllowedException(String msg) {
        super(msg);
    }

    public MethodNotAllowedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getHttpStatus() {
        return 405;
    }

    @Override
    public int getBusinessRejectReason() {
        return BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE;
    }
}

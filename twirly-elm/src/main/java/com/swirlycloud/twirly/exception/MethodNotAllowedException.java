/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.BusinessRejectReason;

/**
 * A request was made of a resource using a request method not supported by that resource; for
 * example, using GET on a form which requires data to be presented via POST, or using PUT on a
 * read-only resource.
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

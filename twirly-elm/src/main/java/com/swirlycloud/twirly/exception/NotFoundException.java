/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import com.swirlycloud.twirly.fix.BusinessRejectReason;

/**
 * The requested resource could not be found but may be available again in the future. Subsequent
 * requests by the client are permissible.
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

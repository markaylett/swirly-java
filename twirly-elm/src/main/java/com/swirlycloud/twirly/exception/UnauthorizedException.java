/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * Similar to 403 Forbidden, but specifically for use when authentication is required and has failed
 * or has not yet been provided.
 * 
 * @author Mark Aylett
 */
public final class UnauthorizedException extends ServException {

    private static final long serialVersionUID = 1L;
    private static final int NUM = 403;

    public UnauthorizedException(String msg) {
        super(NUM, msg);
    }

    public UnauthorizedException(String msg, Throwable cause) {
        super(NUM, msg, cause);
    }
}

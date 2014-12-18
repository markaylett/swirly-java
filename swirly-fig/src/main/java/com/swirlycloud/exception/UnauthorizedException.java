/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.exception;

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

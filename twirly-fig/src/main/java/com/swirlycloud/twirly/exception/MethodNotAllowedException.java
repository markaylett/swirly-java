/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

public final class MethodNotAllowedException extends ServException {

    private static final long serialVersionUID = 1L;
    private static final int NUM = 405;

    public MethodNotAllowedException(String msg) {
        super(NUM, msg);
    }

    public MethodNotAllowedException(String msg, Throwable cause) {
        super(NUM, msg, cause);
    }
}

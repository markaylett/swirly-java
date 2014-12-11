/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.exception;

public final class BadRequestException extends ServException {

    private static final long serialVersionUID = 1L;
    private static final int NUM = 400;

    public BadRequestException(String msg) {
        super(NUM, msg);
    }

    public BadRequestException(String msg, Throwable cause) {
        super(NUM, msg, cause);
    }
}
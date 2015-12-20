/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

/**
 * The request could not be understood by the server due to malformed syntax. The client SHOULD NOT
 * repeat the request without modifications.
 *
 * @author Mark Aylett
 */
public class BadRequestException extends ServException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getHttpStatus() {
        return 400;
    }
}

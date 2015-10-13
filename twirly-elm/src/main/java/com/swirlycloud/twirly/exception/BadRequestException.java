/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * The server cannot or will not process the request due to something that is perceived to be a
 * client error (e.g., malformed request syntax, invalid request message framing, or deceptive
 * request routing).
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

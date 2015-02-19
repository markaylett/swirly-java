/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * The server cannot or will not process the request due to something that is perceived to be a
 * client error (e.g., malformed request syntax, invalid request message framing, or deceptive
 * request routing).
 * 
 * @author Mark Aylett
 */
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

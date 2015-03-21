/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * The requested resource could not be found but may be available again in the future. Subsequent
 * requests by the client are permissible.
 * 
 * @author Mark Aylett
 */
public final class NotFoundException extends ServException {

    private static final long serialVersionUID = 1L;
    private static final int NUM = 404;

    public NotFoundException(String msg) {
        super(NUM, msg);
    }

    public NotFoundException(String msg, Throwable cause) {
        super(NUM, msg, cause);
    }
}

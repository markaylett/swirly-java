/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * The request was a valid request, but the server is refusing to respond to it. Unlike a 401
 * Unauthorized response, authenticating will make no difference.
 * 
 * @author Mark Aylett
 */
public final class ForbiddenException extends ServException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException(String msg) {
        super(msg);
    }

    public ForbiddenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getHttpStatus() {
        return 403;
    }
}

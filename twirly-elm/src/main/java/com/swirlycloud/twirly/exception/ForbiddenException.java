/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * The server understood the request, but is refusing to fulfill it. Authorization will not help and
 * the request SHOULD NOT be repeated. If the request method was not HEAD and the server wishes to
 * make public why the request has not been fulfilled, it SHOULD describe the reason for the refusal
 * in the entity. If the server does not wish to make this information available to the client, the
 * status code 404 (Not Found) can be used instead.
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

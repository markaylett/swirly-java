/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * The server is currently unavailable (because it is overloaded or down for maintenance).
 * Generally, this is a temporary state.
 * 
 * @author Mark Aylett
 */
public final class ServiceUnavailableException extends ServException {

    private static final long serialVersionUID = 1L;
    private static final int NUM = 503;

    public ServiceUnavailableException(String msg) {
        super(NUM, msg);
    }

    public ServiceUnavailableException(String msg, Throwable cause) {
        super(NUM, msg, cause);
    }
}

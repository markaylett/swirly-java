/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * The server encountered an unexpected condition which prevented it from fulfilling the request.
 * 
 * @author Mark Aylett
 */
public class InternalException extends ServException {

    private static final long serialVersionUID = 1L;

    public InternalException(String msg) {
        super(msg);
    }

    public InternalException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getHttpStatus() {
        return 500;
    }
}

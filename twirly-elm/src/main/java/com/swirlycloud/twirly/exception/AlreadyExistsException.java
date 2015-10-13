/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

public class AlreadyExistsException extends BadRequestException {

    private static final long serialVersionUID = 1L;

    public AlreadyExistsException(String msg) {
        super(msg);
    }

    public AlreadyExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

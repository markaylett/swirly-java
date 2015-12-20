/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

public class InvalidException extends BadRequestException {

    private static final long serialVersionUID = 1L;

    public InvalidException(String msg) {
        super(msg);
    }

    public InvalidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

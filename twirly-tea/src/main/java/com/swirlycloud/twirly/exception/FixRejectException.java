/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

public final class FixRejectException extends Exception {

    private static final long serialVersionUID = 1L;

    public FixRejectException(String message) {
        super(message);
    }

    public FixRejectException(String message, Throwable cause) {
        super(message, cause);
    }
}

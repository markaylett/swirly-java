/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

public final class UncheckedExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UncheckedExecutionException(Throwable cause) {
        super(cause);
    }

    public UncheckedExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

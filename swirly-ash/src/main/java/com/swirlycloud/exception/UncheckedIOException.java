/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.exception;

public final class UncheckedIOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UncheckedIOException(Throwable cause) {
        super(cause);
    }

    public UncheckedIOException(String message, Throwable cause) {
        super(message, cause);
    }
}

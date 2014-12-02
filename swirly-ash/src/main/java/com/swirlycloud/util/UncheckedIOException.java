/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

public final class UncheckedIOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UncheckedIOException(Throwable cause) {
        super(cause);
    }

    public UncheckedIOException(String message, Throwable cause) {
        super(message, cause);
    }
}

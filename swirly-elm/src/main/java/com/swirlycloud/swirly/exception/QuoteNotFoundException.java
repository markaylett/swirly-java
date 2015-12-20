/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

public class QuoteNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public QuoteNotFoundException(String msg) {
        super(msg);
    }

    public QuoteNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

public class LiquidityUnavailableException extends InternalException {

    private static final long serialVersionUID = 1L;

    public LiquidityUnavailableException(String msg) {
        super(msg);
    }

    public LiquidityUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

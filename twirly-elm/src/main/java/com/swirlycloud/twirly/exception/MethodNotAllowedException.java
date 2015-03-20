/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

/**
 * A request was made of a resource using a request method not supported by that resource; for
 * example, using GET on a form which requires data to be presented via POST, or using PUT on a
 * read-only resource.
 * 
 * @author Mark Aylett
 */
public final class MethodNotAllowedException extends ServException {

    private static final long serialVersionUID = 1L;
    private static final int NUM = 405;

    public MethodNotAllowedException(String msg) {
        super(NUM, msg);
    }

    public MethodNotAllowedException(String msg, Throwable cause) {
        super(NUM, msg, cause);
    }
}

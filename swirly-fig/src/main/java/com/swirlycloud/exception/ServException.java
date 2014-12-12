/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.exception;

import java.io.IOException;

import com.swirlycloud.util.Jsonifiable;

public abstract class ServException extends Exception implements Jsonifiable {

    private static final long serialVersionUID = 1L;

    private final int num;

    public ServException(int num, String msg) {
        super(msg);
        this.num = num;
    }

    public ServException(int num, String msg, Throwable cause) {
        super(msg, cause);
        this.num = num;
    }

    @Override
    public final void toJson(Appendable out) throws IOException {
        out.append("{\"num\":");
        out.append(String.valueOf(num));
        out.append(",\"msg\":\"");
        out.append(getMessage());
        out.append("\"}");
    }

    public final int getNum() {
        return num;
    }
}

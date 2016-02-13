/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.fix.BusinessRejectReason;
import com.swirlycloud.swirly.fix.CancelRejectReason;
import com.swirlycloud.swirly.fix.OrderRejectReason;
import com.swirlycloud.swirly.util.Jsonable;
import com.swirlycloud.swirly.util.Params;

public abstract class ServException extends Exception implements Jsonable {

    private static final long serialVersionUID = 1L;

    public ServException(String msg) {
        super(msg);
    }

    public ServException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public final void toJson(@Nullable Params params, @NonNull Appendable out) throws IOException {
        out.append("{\"num\":");
        out.append(String.valueOf(getHttpStatus()));
        out.append(",\"msg\":\"");
        out.append(getMessage());
        out.append("\"}");
    }

    public abstract int getHttpStatus();

    public int getBusinessRejectReason() {
        return BusinessRejectReason.OTHER;
    }

    public int getCancelRejectReason() {
        return CancelRejectReason.OTHER;
    }

    public int getOrderRejectReason() {
        return OrderRejectReason.OTHER;
    }
}

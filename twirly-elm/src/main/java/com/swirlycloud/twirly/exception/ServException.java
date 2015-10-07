/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.fix.BusinessRejectReason;
import com.swirlycloud.twirly.fix.CancelRejectReason;
import com.swirlycloud.twirly.fix.OrderRejectReason;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public class ServException extends Exception implements Jsonifiable {

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

    public int getHttpStatus() {
        // Internal Server Error.
        return 500;
    }

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

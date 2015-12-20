/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.entity.Financial;
import com.swirlycloud.swirly.entity.Posn;
import com.swirlycloud.swirly.node.RbNode;
import com.swirlycloud.swirly.util.Params;

public final @NonNullByDefault class RestUtil {
    private RestUtil() {
    }

    public static boolean getExpiredParam(Params params) {
        final Boolean val = params.getParam("expired", Boolean.class);
        return val == null ? false : val.booleanValue();
    }

    public static void filterMarket(@Nullable RbNode first, String market, Params params,
            Appendable out) throws IOException {
        out.append('[');
        int i = 0;
        for (RbNode node = first; node != null; node = node.rbNext()) {
            final Financial fin = (Financial) node;
            if (!fin.getMarket().equals(market)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            fin.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    public static void filterPosn(@Nullable RbNode first, String contr, Params params,
            Appendable out) throws IOException {
        out.append('[');
        int i = 0;
        for (RbNode node = first; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (!posn.getContr().equals(contr)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            posn.toJson(params, out);
            ++i;
        }
        out.append(']');
    }
}

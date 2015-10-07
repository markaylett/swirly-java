/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.MarketView;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class RestUtil {
    private RestUtil() {
    }

    public static boolean getExpiredParam(Params params) {
        final Boolean val = params.getParam("expired", Boolean.class);
        return val == null ? false : val.booleanValue();
    }

    public static boolean getViewsParam(Params params) {
        final Boolean val = params.getParam("views", Boolean.class);
        return val == null ? false : val.booleanValue();
    }

    public static void getView(@Nullable RbNode first, String market, Params params, Appendable out)
            throws IOException {
        out.append('[');
        int i = 0;
        for (RbNode node = first; node != null; node = node.rbNext()) {
            final MarketView view = (MarketView) node;
            if (!view.getMarket().equals(market)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            view.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    public static void getOrder(@Nullable RbNode first, String market, Params params,
            Appendable out) throws IOException {
        out.append('[');
        int i = 0;
        for (RbNode node = first; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (!order.getMarket().equals(market)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            order.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    public static void getTrade(@Nullable RbNode first, String market, Params params,
            Appendable out) throws IOException {
        out.append('[');
        int i = 0;
        for (RbNode node = first; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (!trade.getMarket().equals(market)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            trade.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    public static void getPosn(@Nullable RbNode first, String contr, Params params, Appendable out)
            throws IOException {
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

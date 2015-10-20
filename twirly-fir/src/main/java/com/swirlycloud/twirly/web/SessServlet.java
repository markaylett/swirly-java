/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.StringUtil.splitPath;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public class SessServlet extends RestServlet {

    protected static final int TYPE_PART = 0;
    protected static final int MARKET_PART = 1;
    protected static final int ID_PART = 2;
    protected static final int CONTR_PART = 1;
    protected static final int SETTL_DATE_PART = 2;

    @SuppressWarnings("null")
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserSignedIn(req)) {
                throw new UnauthorizedException("user is not logged-in");
            }
            final String trader = getTrader(req);

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final Params params = newParams(req);
            final long now = now();
            long timeout = -1;
            if (parts.length == 0) {
                timeout = rest.getSess(trader, params, now, resp.getWriter());
            } else if ("order".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    timeout = rest.getOrder(trader, params, now, resp.getWriter());
                } else if (parts.length == 2) {
                    timeout = rest.getOrder(trader, parts[MARKET_PART], params, now, resp.getWriter());
                } else if (parts.length == 3) {
                    timeout = rest.getOrder(trader, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                            params, now, resp.getWriter());
                }
            } else if ("trade".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    timeout = rest.getTrade(trader, params, now, resp.getWriter());
                } else if (parts.length == 2) {
                    timeout = rest.getTrade(trader, parts[MARKET_PART], params, now, resp.getWriter());
                } else if (parts.length == 3) {
                    timeout = rest.getTrade(trader, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                            params, now, resp.getWriter());
                }
            } else if ("posn".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    timeout = rest.getPosn(trader, params, now, resp.getWriter());
                } else if (parts.length == 2) {
                    timeout = rest.getPosn(trader, parts[CONTR_PART], params, now, resp.getWriter());
                } else if (parts.length == 3) {
                    timeout = rest.getPosn(trader, parts[CONTR_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), params, now,
                            resp.getWriter());
                }
            }

            if (timeout == -1) {
                throw new NotFoundException("resource does not exist");
            }
            sendJsonResponse(resp, timeout);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}
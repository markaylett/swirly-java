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
            boolean match = false;
            if (parts.length == 0) {
                rest.getSess(trader, params, now, resp.getWriter());
                match = true;
            } else if ("order".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getOrder(trader, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getOrder(trader, parts[MARKET_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getOrder(trader, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                            params, now, resp.getWriter());
                    match = true;
                }
            } else if ("trade".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getTrade(trader, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getTrade(trader, parts[MARKET_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getTrade(trader, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                            params, now, resp.getWriter());
                    match = true;
                }
            } else if ("posn".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getPosn(trader, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getPosn(trader, parts[CONTR_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getPosn(trader, parts[CONTR_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), params, now,
                            resp.getWriter());
                    match = true;
                }
            } else if ("quote".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getQuote(trader, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getQuote(trader, parts[MARKET_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getQuote(trader, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                            params, now, resp.getWriter());
                    match = true;
                }
            }

            if (!match) {
                throw new NotFoundException("resource does not exist");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}
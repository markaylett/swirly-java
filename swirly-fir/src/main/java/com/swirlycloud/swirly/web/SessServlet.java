/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import static com.swirlycloud.swirly.util.StringUtil.splitPath;
import static com.swirlycloud.swirly.util.TimeUtil.getTimeOfDay;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.swirly.entity.EntitySet;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServException;
import com.swirlycloud.swirly.exception.UnauthorizedException;
import com.swirlycloud.swirly.util.Params;

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
            final long now = getTimeOfDay();
            boolean match = false;
            if (parts.length == 0) {
                int bs = EntitySet.ORDER | EntitySet.TRADE | EntitySet.POSN | EntitySet.QUOTE
                        | EntitySet.VIEW;
                final EntitySet es = new EntitySet(bs);
                rest.getSess(trader, es, params, now, resp.getWriter());
                match = true;
            } else {
                final EntitySet es = EntitySet.parse(parts[TYPE_PART]);
                if (!es.isRecSet()) {
                    if (es.hasMany()) {
                        if (parts.length == 1) {
                            rest.getSess(trader, es, params, now, resp.getWriter());
                            match = true;
                        }
                    } else {
                        switch (es.getFirst()) {
                        case EntitySet.ORDER:
                            if (parts.length == 1) {
                                rest.getOrder(trader, params, now, resp.getWriter());
                                match = true;
                            } else if (parts.length == 2) {
                                rest.getOrder(trader, parts[MARKET_PART], params, now,
                                        resp.getWriter());
                                match = true;
                            } else if (parts.length == 3) {
                                rest.getOrder(trader, parts[MARKET_PART],
                                        Long.parseLong(parts[ID_PART]), params, now,
                                        resp.getWriter());
                                match = true;
                            }
                            break;
                        case EntitySet.TRADE:
                            if (parts.length == 1) {
                                rest.getTrade(trader, params, now, resp.getWriter());
                                match = true;
                            } else if (parts.length == 2) {
                                rest.getTrade(trader, parts[MARKET_PART], params, now,
                                        resp.getWriter());
                                match = true;
                            } else if (parts.length == 3) {
                                rest.getTrade(trader, parts[MARKET_PART],
                                        Long.parseLong(parts[ID_PART]), params, now,
                                        resp.getWriter());
                                match = true;
                            }
                            break;
                        case EntitySet.POSN:
                            if (parts.length == 1) {
                                rest.getPosn(trader, params, now, resp.getWriter());
                                match = true;
                            } else if (parts.length == 2) {
                                rest.getPosn(trader, parts[CONTR_PART], params, now,
                                        resp.getWriter());
                                match = true;
                            } else if (parts.length == 3) {
                                rest.getPosn(trader, parts[CONTR_PART],
                                        Integer.parseInt(parts[SETTL_DATE_PART]), params, now,
                                        resp.getWriter());
                                match = true;
                            }
                            break;
                        case EntitySet.QUOTE:
                            if (parts.length == 1) {
                                rest.getQuote(trader, params, now, resp.getWriter());
                                match = true;
                            } else if (parts.length == 2) {
                                rest.getQuote(trader, parts[MARKET_PART], params, now,
                                        resp.getWriter());
                                match = true;
                            } else if (parts.length == 3) {
                                rest.getQuote(trader, parts[MARKET_PART],
                                        Long.parseLong(parts[ID_PART]), params, now,
                                        resp.getWriter());
                                match = true;
                            }
                            break;
                        case EntitySet.VIEW:
                            if (parts.length == 1) {
                                rest.getView(params, now, resp.getWriter());
                                match = true;
                            } else if (parts.length == 2) {
                                rest.getView(parts[MARKET_PART], params, now, resp.getWriter());
                                match = true;
                            }
                            break;
                        }
                    }
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

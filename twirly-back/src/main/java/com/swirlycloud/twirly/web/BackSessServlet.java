/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.twirly.util.StringUtil.splitPath;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.rest.BackRest;
import com.swirlycloud.twirly.rest.Request;

@SuppressWarnings("serial")
public final class BackSessServlet extends SessServlet {
    @SuppressWarnings("null")
    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final BackRest rest = (BackRest) RestServlet.rest;
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
            final long now = now();

            boolean match = false;
            if (parts.length > 0) {
                if ("order".equals(parts[TYPE_PART])) {
                    if (parts.length == 3) {
                        rest.deleteOrder(trader, parts[MARKET_PART],
                                Long.parseLong(parts[ID_PART]), now);
                        match = true;
                    }
                } else if ("trade".equals(parts[TYPE_PART])) {
                    if (parts.length == 3) {
                        rest.deleteTrade(trader, parts[MARKET_PART],
                                Long.parseLong(parts[ID_PART]), now);
                        match = true;
                    }
                }
            }

            if (!match) {
                throw new NotFoundException("resource does not exist");
            }
            resp.setHeader("Cache-Control", "no-cache");
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }

    @SuppressWarnings("null")
    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final BackRest rest = (BackRest) RestServlet.rest;
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

            if (parts.length != 2) {
                throw new MethodNotAllowedException("post is not allowed on this resource");
            }
            final String market = parts[MARKET_PART];
            final long now = now();

            final Request r = parseRequest(req);
            if ("order".equals(parts[TYPE_PART])) {

                final int required = Request.SIDE | Request.TICKS | Request.LOTS;
                final int optional = Request.REF | Request.MIN_LOTS;
                if (!r.isValid(required, optional)) {
                    throw new BadRequestException("request fields are invalid");
                }
                rest.postOrder(trader, market, r.getRef(), r.getSide(), r.getTicks(), r.getLots(),
                        r.getMinLots(), PARAMS_NONE, now, resp.getWriter());
            } else if ("trade".equals(parts[TYPE_PART])) {

                final int required = Request.TRADER | Request.SIDE | Request.TICKS | Request.LOTS;
                final int optional = Request.REF | Request.ROLE | Request.CPTY;
                if (!r.isValid(required, optional)) {
                    throw new BadRequestException("request fields are invalid");
                }

                if (!realm.isUserAdmin(req)) {
                    throw new BadRequestException("user is not an admin");
                }
                rest.postTrade(r.getTrader(), market, r.getRef(), r.getSide(), r.getTicks(),
                        r.getLots(), r.getRole(), r.getCpty(), PARAMS_NONE, now, resp.getWriter());
            } else {
                throw new NotFoundException("resource does not exist");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }

    @SuppressWarnings("null")
    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final BackRest rest = (BackRest) RestServlet.rest;
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

            if (parts.length != 3 || !"order".equals(parts[TYPE_PART])) {
                throw new MethodNotAllowedException("put is not allowed on this resource");
            }
            final String market = parts[MARKET_PART];
            final long id = Long.parseLong(parts[ID_PART]);

            final Request r = parseRequest(req);
            if (!r.isLotsSet()) {
                throw new BadRequestException("request fields are invalid");
            }
            final long now = now();
            rest.putOrder(trader, market, id, r.getLots(), PARAMS_NONE, now, resp.getWriter());
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

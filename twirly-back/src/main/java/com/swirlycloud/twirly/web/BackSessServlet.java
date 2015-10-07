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

import com.swirlycloud.twirly.domain.MarketId;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
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

            if (parts.length != 3) {
                throw new MethodNotAllowedException("not allowed on this resource");
            }
            final String market = parts[MARKET_PART];
            final MarketId ids = MarketId.parse(market, parts[ID_PART]);
            if (ids == null) {
                throw new BadRequestException("id not specified");
            }
            final long now = now();
            if (ids.jslNext() != null) {
                if ("order".equals(parts[TYPE_PART])) {
                    rest.deleteOrder(trader, market, ids, now);
                } else if ("trade".equals(parts[TYPE_PART])) {
                    rest.deleteTrade(trader, market, ids, now);
                } else {
                    throw new MethodNotAllowedException("not allowed on this resource");
                }
            } else {
                final long id = ids.getId();
                if ("order".equals(parts[TYPE_PART])) {
                    rest.deleteOrder(trader, market, id, now);
                } else if ("trade".equals(parts[TYPE_PART])) {
                    rest.deleteTrade(trader, market, id, now);
                } else {
                    throw new MethodNotAllowedException("not allowed on this resource");
                }
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
                throw new MethodNotAllowedException("not allowed on this resource");
            }
            final String market = parts[MARKET_PART];
            final Request r = parseRequest(req);
            final long now = now();
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
                throw new MethodNotAllowedException("not allowed on this resource");
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
            final MarketId ids = MarketId.parse(market, parts[ID_PART]);
            if (ids == null) {
                throw new BadRequestException("id not specified");
            }
            final Request r = parseRequest(req);
            if (!r.isLotsSet()) {
                throw new BadRequestException("request fields are invalid");
            }
            final long now = now();
            if (ids.jslNext() != null) {
                rest.putOrder(trader, market, ids, r.getLots(), PARAMS_NONE, now, resp.getWriter());
            } else {
                final long id = ids.getId();
                rest.putOrder(trader, market, id, r.getLots(), PARAMS_NONE, now, resp.getWriter());
            }

            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.swirly.util.StringUtil.splitPath;
import static com.swirlycloud.swirly.util.TimeUtil.now;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.swirly.domain.MarketId;
import com.swirlycloud.swirly.exception.ForbiddenException;
import com.swirlycloud.swirly.exception.InvalidException;
import com.swirlycloud.swirly.exception.MethodNotAllowedException;
import com.swirlycloud.swirly.exception.ServException;
import com.swirlycloud.swirly.exception.UnauthorizedException;
import com.swirlycloud.swirly.rest.BackRest;
import com.swirlycloud.swirly.rest.RestRequest;

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
                throw new InvalidException("id not specified");
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
            setNoContent(resp);
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
            final RestRequest r = parseRequest(req);
            final long now = now();
            if ("order".equals(parts[TYPE_PART])) {

                final int required = RestRequest.SIDE | RestRequest.LOTS | RestRequest.TICKS;
                final int optional = RestRequest.REF | RestRequest.QUOTE_ID | RestRequest.MIN_LOTS;
                if (!r.isValid(required, optional)) {
                    throw new InvalidException("request fields are invalid");
                }
                rest.postOrder(trader, market, r.getRef(), r.getQuoteId(), r.getSide(), r.getLots(),
                        r.getTicks(), r.getMinLots(), PARAMS_NONE, now, resp.getWriter());
            } else if ("trade".equals(parts[TYPE_PART])) {

                final int required = RestRequest.TRADER | RestRequest.SIDE | RestRequest.LOTS
                        | RestRequest.TICKS;
                final int optional = RestRequest.REF | RestRequest.ROLE | RestRequest.CPTY;
                if (!r.isValid(required, optional)) {
                    throw new InvalidException("request fields are invalid");
                }

                if (!realm.isUserAdmin(req)) {
                    throw new ForbiddenException("user is not an admin");
                }
                rest.postTrade(r.getTrader(), market, r.getRef(), r.getSide(), r.getLots(),
                        r.getTicks(), r.getRole(), r.getCpty(), PARAMS_NONE, now, resp.getWriter());
            } else if ("quote".equals(parts[TYPE_PART])) {

                final int required = RestRequest.SIDE | RestRequest.LOTS;
                final int optional = RestRequest.REF;
                if (!r.isValid(required, optional)) {
                    throw new InvalidException("request fields are invalid");
                }
                rest.postQuote(trader, market, r.getRef(), r.getSide(), r.getLots(), PARAMS_NONE,
                        now, resp.getWriter());
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
                throw new InvalidException("id not specified");
            }
            final RestRequest r = parseRequest(req);
            if (!r.isLotsSet()) {
                throw new InvalidException("request fields are invalid");
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.twirly.util.StringUtil.splitPath;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.ForbiddenException;
import com.swirlycloud.twirly.exception.InvalidException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.rest.BackRest;
import com.swirlycloud.twirly.rest.RestRequest;

@SuppressWarnings("serial")
public final class BackRecServlet extends RecServlet {

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

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 1) {
                throw new MethodNotAllowedException("not allowed on this resource");
            }

            final RestRequest r = parseRequest(req);
            final long now = now();
            if ("market".equals(parts[TYPE_PART])) {

                final int required = RestRequest.MNEM | RestRequest.DISPLAY | RestRequest.CONTR;
                final int optional = RestRequest.SETTL_DATE | RestRequest.EXPIRY_DATE | RestRequest.STATE;
                if (!r.isValid(required, optional)) {
                    throw new InvalidException("request fields are invalid");
                }

                if (!realm.isUserAdmin(req)) {
                    throw new ForbiddenException("user is not an admin");
                }
                rest.postMarket(r.getMnem(), r.getDisplay(), r.getContr(), r.getSettlDate(),
                        r.getExpiryDate(), r.getState(), PARAMS_NONE, now, resp.getWriter());

            } else if ("trader".equals(parts[TYPE_PART])) {

                final int required = RestRequest.MNEM | RestRequest.DISPLAY;
                final int optional = RestRequest.EMAIL;
                if (!r.isValid(required, optional)) {
                    throw new InvalidException("request fields are invalid");
                }

                String email = realm.getUserEmail(req);
                if (r.isEmailSet() && !r.getEmail().equals(email)) {
                    if (!realm.isUserAdmin(req)) {
                        throw new ForbiddenException("user is not an admin");
                    }
                    email = r.getEmail();
                }
                rest.postTrader(r.getMnem(), r.getDisplay(), email, PARAMS_NONE, now,
                        resp.getWriter());

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
            throws IOException {
        final BackRest rest = (BackRest) RestServlet.rest;
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserSignedIn(req)) {
                throw new UnauthorizedException("user is not logged-in");
            }

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 1) {
                throw new MethodNotAllowedException("not allowed on this resource");
            }

            final RestRequest r = parseRequest(req);
            final long now = now();
            if ("market".equals(parts[TYPE_PART])) {

                final int required = RestRequest.MNEM | RestRequest.DISPLAY | RestRequest.STATE;
                if (!r.isValid(required)) {
                    throw new InvalidException("request fields are invalid");
                }

                if (!realm.isUserAdmin(req)) {
                    throw new ForbiddenException("user is not an admin");
                }
                rest.putMarket(r.getMnem(), r.getDisplay(), r.getState(), PARAMS_NONE, now,
                        resp.getWriter());

            } else if ("trader".equals(parts[TYPE_PART])) {

                final int required = RestRequest.MNEM | RestRequest.DISPLAY;
                final int optional = RestRequest.EMAIL;
                if (!r.isValid(required, optional)) {
                    throw new InvalidException("request fields are invalid");
                }

                String email = realm.getUserEmail(req);
                if (r.isEmailSet() && !email.equals(r.getEmail())) {
                    if (!realm.isUserAdmin(req)) {
                        throw new ForbiddenException("user is not an admin");
                    }
                    email = r.getEmail();
                }
                rest.putTrader(r.getMnem(), r.getDisplay(), PARAMS_NONE, now, resp.getWriter());

            } else {
                throw new MethodNotAllowedException("not allowed on this resource");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

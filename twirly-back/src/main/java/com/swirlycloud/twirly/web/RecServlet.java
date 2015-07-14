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

import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.ForbiddenException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public final class RecServlet extends RestServlet {

    private static final int TYPE_PART = 0;
    private static final int MNEM_PART = 1;

    @SuppressWarnings("null")
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserSignedIn(req)) {
                throw new UnauthorizedException("user is not logged-in");
            }

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final Params params = newParams(req);
            final long now = now();

            boolean match = false;
            if (parts.length == 0) {
                rest.getRec(realm.isUserAdmin(req), params, now, resp.getWriter());
                match = true;
            } else if ("asset".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getRec(RecType.ASSET, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getRec(RecType.ASSET, parts[MNEM_PART], params, now, resp.getWriter());
                    match = true;
                }
            } else if ("contr".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getRec(RecType.CONTR, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getRec(RecType.CONTR, parts[MNEM_PART], params, now, resp.getWriter());
                    match = true;
                }
            } else if ("market".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getRec(RecType.MARKET, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getRec(RecType.MARKET, parts[MNEM_PART], params, now, resp.getWriter());
                    match = true;
                }
            } else if ("trader".equals(parts[TYPE_PART])) {
                if (!realm.isUserAdmin(req)) {
                    throw new BadRequestException("user is not an admin");
                }
                if (parts.length == 1) {
                    rest.getRec(RecType.TRADER, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getRec(RecType.TRADER, parts[MNEM_PART], params, now, resp.getWriter());
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
                throw new MethodNotAllowedException("post is not allowed on this resource");
            }

            final Request r = parseRequest(req);
            if ("market".equals(parts[TYPE_PART])) {

                final int required = Request.MNEM | Request.DISPLAY | Request.CONTR;
                final int optional = Request.SETTL_DATE | Request.EXPIRY_DATE | Request.STATE;
                if (!r.isValid(required, optional)) {
                    throw new BadRequestException("request fields are invalid");
                }

                if (!realm.isUserAdmin(req)) {
                    throw new BadRequestException("user is not an admin");
                }
                final long now = now();
                rest.postMarket(r.getMnem(), r.getDisplay(), r.getContr(), r.getSettlDate(),
                        r.getExpiryDate(), r.getState(), PARAMS_NONE, now, resp.getWriter());

            } else if ("trader".equals(parts[TYPE_PART])) {

                final int required = Request.MNEM | Request.DISPLAY;
                final int optional = Request.EMAIL;
                if (!r.isValid(required, optional)) {
                    throw new BadRequestException("request fields are invalid");
                }

                String email = realm.getUserEmail(req);
                if (r.isEmailSet()) {
                    if (!email.equals(r.getEmail()) && !realm.isUserAdmin(req)) {
                        throw new ForbiddenException("user is not an admin");
                    }
                    email = r.getEmail();
                }
                final long now = now();
                rest.postTrader(r.getMnem(), r.getDisplay(), email, PARAMS_NONE, now,
                        resp.getWriter());

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
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                throw new MethodNotAllowedException("put is not allowed on this resource");
            }

            final Request r = parseRequest(req);
            if ("market".equals(parts[TYPE_PART])) {

                final int required = Request.MNEM | Request.DISPLAY | Request.STATE;
                if (!r.isValid(required)) {
                    throw new BadRequestException("request fields are invalid");
                }

                if (!realm.isUserAdmin(req)) {
                    throw new BadRequestException("user is not an admin");
                }
                final long now = now();
                rest.putMarket(r.getMnem(), r.getDisplay(), r.getState(), PARAMS_NONE, now,
                        resp.getWriter());

            } else if ("trader".equals(parts[TYPE_PART])) {

                final int required = Request.MNEM | Request.DISPLAY;
                final int optional = Request.EMAIL;
                if (!r.isValid(required, optional)) {
                    throw new BadRequestException("request fields are invalid");
                }

                String email = realm.getUserEmail(req);
                if (r.isEmailSet()) {
                    if (!email.equals(r.getEmail()) && !realm.isUserAdmin(req)) {
                        throw new ForbiddenException("user is not an admin");
                    }
                    email = r.getEmail();
                }
                final long now = now();
                rest.putTrader(r.getMnem(), r.getDisplay(), PARAMS_NONE, now, resp.getWriter());

            } else {
                throw new NotFoundException("resource does not exist");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

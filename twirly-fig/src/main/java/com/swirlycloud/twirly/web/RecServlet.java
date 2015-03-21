/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.twirly.util.StringUtil.splitPath;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (realm.isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserLoggedIn()) {
                throw new UnauthorizedException("user is not logged-in");
            }

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final Params params = newParams(req);
            final long now = System.currentTimeMillis();

            boolean match = false;
            if (parts.length == 0) {
                rest.getRec(realm.isUserAdmin(), params, now, resp.getWriter());
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
                if (!realm.isUserAdmin()) {
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

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (realm.isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserLoggedIn()) {
                throw new UnauthorizedException("user is not logged-in");
            }

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 1) {
                throw new MethodNotAllowedException("post is not allowed on this resource");
            }

            final Request r = parseRequest(req);
            if ("market".equals(parts[TYPE_PART])) {

                if (!realm.isUserAdmin()) {
                    throw new BadRequestException("user is not an admin");
                }

                if (r.getFields() != (Request.MNEM | Request.DISPLAY | Request.CONTR
                        | Request.SETTL_DATE | Request.EXPIRY_DATE)) {
                    throw new BadRequestException("request fields are invalid");
                }
                final long now = System.currentTimeMillis();
                rest.postMarket(r.getMnem(), r.getDisplay(), r.getContr(), r.getSettlDate(),
                        r.getExpiryDate(), PARAMS_NONE, now, resp.getWriter());

            } else if ("trader".equals(parts[TYPE_PART])) {

                String email = realm.getUserEmail();

                int fields = r.getFields();
                if ((fields & Request.EMAIL) != 0) {
                    if (!r.getEmail().equals(email) && !realm.isUserAdmin()) {
                        throw new ForbiddenException("user is not an admin");
                    }
                    fields &= ~Request.EMAIL;
                    email = r.getEmail();
                }
                if (fields != (Request.MNEM | Request.DISPLAY)) {
                    throw new BadRequestException("request fields are invalid");
                }
                final long now = System.currentTimeMillis();
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
}

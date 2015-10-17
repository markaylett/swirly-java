/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.StringUtil.splitPath;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.ForbiddenException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.rec.RecType;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public class RecServlet extends RestServlet {

    protected static final int TYPE_PART = 0;
    protected static final int MNEM_PART = 1;

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

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final Params params = newParams(req);
            final long now = now();
            long timeout = -1;
            if (parts.length == 0) {
                timeout = rest.getRec(realm.isUserAdmin(req), params, now, resp.getWriter());
            } else if ("asset".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    timeout = rest.getRec(RecType.ASSET, params, now, resp.getWriter());
                } else if (parts.length == 2) {
                    timeout = rest.getRec(RecType.ASSET, parts[MNEM_PART], params, now, resp.getWriter());
                }
            } else if ("contr".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    timeout = rest.getRec(RecType.CONTR, params, now, resp.getWriter());
                } else if (parts.length == 2) {
                    timeout = rest.getRec(RecType.CONTR, parts[MNEM_PART], params, now, resp.getWriter());
                }
            } else if ("market".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    timeout = rest.getRec(RecType.MARKET, params, now, resp.getWriter());
                } else if (parts.length == 2) {
                    timeout = rest.getRec(RecType.MARKET, parts[MNEM_PART], params, now, resp.getWriter());
                }
            } else if ("trader".equals(parts[TYPE_PART])) {
                if (!realm.isUserAdmin(req)) {
                    throw new ForbiddenException("user is not an admin");
                }
                if (parts.length == 1) {
                    timeout = rest.getRec(RecType.TRADER, params, now, resp.getWriter());
                } else if (parts.length == 2) {
                    timeout = rest.getRec(RecType.TRADER, parts[MNEM_PART], params, now, resp.getWriter());
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
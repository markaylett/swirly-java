/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.StringUtil.splitPath;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.entity.EntitySet;
import com.swirlycloud.twirly.entity.RecType;
import com.swirlycloud.twirly.exception.ForbiddenException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public class RecServlet extends RestServlet {

    protected static final int TYPE_PART = 0;
    protected static final int MNEM_PART = 1;

    private static RecType toRecType(int n) {
        RecType type;
        switch (n) {
        case EntitySet.ASSET:
            type = RecType.ASSET;
            break;
        case EntitySet.CONTR:
            type = RecType.CONTR;
            break;
        case EntitySet.MARKET:
            type = RecType.MARKET;
            break;
        case EntitySet.TRADER:
            type = RecType.TRADER;
            break;
        default:
            throw new IllegalArgumentException("invalid side");

        }
        return type;
    }

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
            boolean match = false;
            if (parts.length == 0) {
                int bs = EntitySet.ASSET | EntitySet.CONTR | EntitySet.MARKET;
                if (realm.isUserAdmin(req)) {
                    bs |= EntitySet.TRADER;
                }
                final EntitySet es = new EntitySet(bs);
                rest.getRec(es, params, now, resp.getWriter());
                match = true;
            } else {
                final EntitySet es = EntitySet.parse(parts[TYPE_PART]);
                if (!es.isSessSet()) {
                    if (es.isTraderSet() && !realm.isUserAdmin(req)) {
                        throw new ForbiddenException("user is not an admin");
                    }
                    if (es.hasMany()) {
                        if (parts.length == 1) {
                            rest.getRec(es, params, now, resp.getWriter());
                            match = true;
                        }
                    } else {
                        final RecType type = toRecType(es.getFirst());
                        if (parts.length == 1) {
                            rest.getRec(type, params, now, resp.getWriter());
                            match = true;
                        } else if (parts.length == 2) {
                            rest.getRec(type, parts[MNEM_PART], params, now, resp.getWriter());
                            match = true;
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
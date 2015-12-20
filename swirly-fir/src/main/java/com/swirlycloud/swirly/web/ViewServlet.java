/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import static com.swirlycloud.swirly.util.StringUtil.splitPath;
import static com.swirlycloud.swirly.util.TimeUtil.now;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServException;
import com.swirlycloud.swirly.exception.UnauthorizedException;
import com.swirlycloud.swirly.util.Params;

@SuppressWarnings("serial")
public class ViewServlet extends RestServlet {

    protected static final int MNEM_PART = 0;

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
            if (parts.length == 0) {
                rest.getView(params, now, resp.getWriter());
            } else if (parts.length == 1) {
                rest.getView(parts[MNEM_PART], params, now, resp.getWriter());
            } else {
                throw new NotFoundException("resource does not exist");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.web;

import static com.swirlycloud.util.StringUtil.splitPath;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.exception.NotFoundException;
import com.swirlycloud.exception.ServException;

@SuppressWarnings("serial")
public final class CronServlet extends RestServlet {

    private static final int JOB_PART = 0;

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            @SuppressWarnings("unused")
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            boolean match = false;
            if (parts.length > 0) {
                if ("endofday".equals(parts[JOB_PART])) {
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
}

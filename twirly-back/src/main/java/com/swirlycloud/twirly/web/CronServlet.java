/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.StringUtil.splitPath;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.fx.EcbRates;

@SuppressWarnings("serial")
public final class CronServlet extends RestServlet {

    private static final int JOB_PART = 0;

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (GaeContext.isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            final Rest rest = GaeContext.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final long now = System.currentTimeMillis();

            boolean match = false;
            if (parts.length > 0) {
                if ("endofday".equals(parts[JOB_PART])) {
                    log("processing end-of-day");
                    rest.getEndOfDay(now);
                    match = true;
                } else if ("ecbrates".equals(parts[JOB_PART])) {
                    log("processing ecb-rates");
                    final EcbRates ecbRates = new EcbRates();
                    try {
                        ecbRates.parse();
                        log("EURUSD: " + ecbRates.getRate("EUR", "USD"));
                    } catch (Throwable t) {
                        log("error: " + t.getLocalizedMessage());
                    }
                    match = true;
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
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import static com.swirlycloud.swirly.util.StringUtil.splitPath;
import static com.swirlycloud.swirly.util.TimeUtil.getTimeOfDay;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.swirly.exception.MethodNotAllowedException;
import com.swirlycloud.swirly.exception.ServException;
import com.swirlycloud.swirly.fx.EcbRates;
import com.swirlycloud.swirly.rest.BackRest;

@SuppressWarnings("serial")
public final class BackTaskServlet extends RestServlet {

    private static final int TASK_PART = 0;

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final BackRest rest = (BackRest) RestServlet.rest;
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 1) {
                throw new MethodNotAllowedException("not allowed on this resource");
            }
            final long now = getTimeOfDay();
            if ("endofday".equals(parts[TASK_PART])) {
                log("processing end-of-day");
                rest.endOfDay(now);
            } else if ("ecbrates".equals(parts[TASK_PART])) {
                log("processing ecb-rates");
                final EcbRates ecbRates = new EcbRates();
                try {
                    ecbRates.parse();
                    log("EURUSD: " + ecbRates.getRate("EUR", "USD"));
                } catch (final Throwable t) {
                    log("error: " + t.getMessage());
                }
                rest.poll(now);
            } else if ("poll".equals(parts[TASK_PART])) {
                rest.poll(now);
            } else {
                throw new MethodNotAllowedException("not allowed on this resource");
            }
            setNoContent(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

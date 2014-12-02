/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.util.PathUtil.splitPath;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public final class MarketServlet extends HttpServlet {

    private static final Integer DEPTH = Integer.valueOf(5);

    private static final int CMNEM_PART = 0;
    private static final int SETTL_DATE_PART = 1;

    @Override
    public final void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        final Rest ctx = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);

        boolean found = false;
        if (parts.length == 0) {
            found = ctx.getMarket(resp.getWriter(), DEPTH);
        } else if (parts.length == 1) {
            found = ctx.getMarket(resp.getWriter(), parts[CMNEM_PART], DEPTH);
        } else if (parts.length == 2) {
            found = ctx.getMarket(resp.getWriter(), parts[CMNEM_PART],
                    Integer.parseInt(parts[SETTL_DATE_PART]), DEPTH);
        }

        if (!found) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}

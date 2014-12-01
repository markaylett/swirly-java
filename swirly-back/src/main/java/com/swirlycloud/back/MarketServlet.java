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
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length == 0) {
            ctx.getMarket(sb, DEPTH);
        } else if (parts.length == 1) {
            ctx.getMarket(sb, parts[0], DEPTH);
        } else if (parts.length == 2) {
            if (!ctx.getMarket(sb, parts[0], Integer.parseInt(parts[1]), DEPTH)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().append(sb);
    }
}

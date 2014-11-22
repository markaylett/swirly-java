/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.web;

import static com.swirlycloud.web.WebUtil.splitPathInfo;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public final class BookServlet extends HttpServlet {

    private static final Integer LEVELS = Integer.valueOf(5);

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Rest ctx = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if (parts.length == 0) {
            ctx.getBook(sb, LEVELS);
        } else if (parts.length == 1) {
            ctx.getBook(sb, parts[0], LEVELS);
        } else if (parts.length == 2) {
            if (!ctx.getBook(sb, parts[0], Integer.parseInt(parts[1]), LEVELS)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.addHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().append(sb);
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.doobry.web.WebUtil.splitPathInfo;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public final class BookServlet extends HttpServlet {

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Ctx ctx = Ctx.getInstance();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if (parts.length == 0) {
            ctx.getBook(sb);
        } else if (parts.length == 1) {
            ctx.getBook(sb, parts[1]);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().append(sb);
    }
}

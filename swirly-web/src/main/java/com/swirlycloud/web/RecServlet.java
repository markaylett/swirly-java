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

import com.swirlycloud.domain.Kind;

@SuppressWarnings("serial")
public final class RecServlet extends HttpServlet {

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Rest rest = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if (parts.length == 0) {
            rest.getRec(sb);
        } else if ("asset".equals(parts[0])) {
            if (parts.length == 1) {
                rest.getRec(sb, Kind.ASSET);
            } else if (parts.length == 2) {
                if (!rest.getRec(sb, Kind.ASSET, parts[1])) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else if ("contr".equals(parts[0])) {
            if (parts.length == 1) {
                rest.getRec(sb, Kind.CONTR);
            } else if (parts.length == 2) {
                if (!rest.getRec(sb, Kind.CONTR, parts[1])) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
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

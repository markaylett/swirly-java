/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.doobry.domain.RecType;

@SuppressWarnings("serial")
public final class RecServlet extends HttpServlet {
    private static final Pattern PATTERN = Pattern.compile("/");

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Ctx ctx = Ctx.getInstance();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            ctx.getRec(sb);
        } else {
            final String[] parts = PATTERN.split(pathInfo.substring(1), 2);
            System.out.println("parts[0]=" + parts[0]);
            if (parts[0].equals("asset")) {
                if (parts.length == 1) {
                    ctx.getRec(sb, RecType.ASSET);
                } else {
                    ctx.getRec(sb, RecType.ASSET, parts[1]);
                }
            } else if (parts[0].equals("contr")) {
                if (parts.length == 1) {
                    ctx.getRec(sb, RecType.CONTR);
                } else {
                    ctx.getRec(sb, RecType.CONTR, parts[1]);
                }
            }
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().append(sb);
    }
}

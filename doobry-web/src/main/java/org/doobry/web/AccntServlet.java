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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public final class AccntServlet extends HttpServlet {

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        if (user == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }

        final Ctx ctx = Ctx.getInstance();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if (parts.length == 0) {
            ctx.getAccnt(sb, user.getEmail());
        } else if (parts[0].equals("order")) {
            if (parts.length == 1) {
                ctx.getOrder(sb, user.getEmail());
            } else if (parts.length == 2) {
                if (!ctx.getOrder(sb, user.getEmail(), Integer.parseInt(parts[1]))) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else if (parts[0].equals("trade")) {
            if (parts.length == 1) {
                ctx.getTrade(sb, user.getEmail());
            } else if (parts.length == 2) {
                if (!ctx.getTrade(sb, user.getEmail(), Integer.parseInt(parts[1]))) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else if (parts[0].equals("posn")) {
            if (parts.length == 1) {
                ctx.getPosn(sb, user.getEmail());
            } else if (parts.length == 2) {
                ctx.getPosn(sb, user.getEmail(), parts[1]);
            } else if (parts.length == 3) {
                if (!ctx.getPosn(sb, user.getEmail(), parts[1], Integer.parseInt(parts[2]))) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().append(sb);
    }
}

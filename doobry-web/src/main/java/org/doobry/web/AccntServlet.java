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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public final class AccntServlet extends HttpServlet {
    private static final Pattern PATTERN = Pattern.compile("/");

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final UserService userService = UserServiceFactory.getUserService();
        final User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }

        final String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
        } else {
            final String[] parts = PATTERN.split(pathInfo);
            if (parts[0].equals("order")) {
            } else if (parts[0].equals("trade")) {
            } else if (parts[0].equals("posn")) {
            }
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("email: " + currentUser.getEmail());
        resp.getWriter().println("nickname: " + currentUser.getNickname());
        resp.getWriter().println("user-id: " + currentUser.getUserId());
        resp.getWriter().println(req.getPathInfo());
    }
}

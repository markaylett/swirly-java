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

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.swirlycloud.domain.Kind;

@SuppressWarnings("serial")
public final class RecServlet extends HttpServlet {

    @Override
    public final void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        final Rest rest = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
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
        } else if ("user".equals(parts[0])) {
            if (parts.length == 1) {
                rest.getRec(sb, Kind.USER);
            } else if (parts.length == 2) {
                if (!rest.getRec(sb, Kind.USER, parts[1])) {
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
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().append(sb);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        if (user == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }
        final String email = user.getEmail();

        final Rest rest = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length != 1 || !"user".equals(parts[0])) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final JSONParser p = new JSONParser();
        final Request r = new Request();
        try {
            p.parse(req.getReader(), r);
        } catch (ParseException e) {
            throw new IOException(e);
        }
        if (r.getFields() != (Request.MNEM | Request.DISPLAY)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        rest.registerUser(sb, r.getMnem(), r.getDisplay(), email);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
        log(sb.toString());
        resp.getWriter().append(sb);
    }
}

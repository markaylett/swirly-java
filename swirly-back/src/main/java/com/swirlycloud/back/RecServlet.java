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
import com.swirlycloud.domain.RecType;

@SuppressWarnings("serial")
public final class RecServlet extends HttpServlet {

    private static final int TYPE_PART = 0;
    private static final int CMNEM_PART = 1;

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

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);

        boolean found = false;
        if (parts.length == 0) {
            found = rest.getRec(resp.getWriter());
        } else if ("asset".equals(parts[TYPE_PART])) {
            if (parts.length == 1) {
                found = rest.getRec(RecType.ASSET, resp.getWriter());
            } else if (parts.length == 2) {
                found = rest.getRec(RecType.ASSET, parts[CMNEM_PART], resp.getWriter());
            }
        } else if ("contr".equals(parts[TYPE_PART])) {
            if (parts.length == 1) {
                found = rest.getRec(RecType.CONTR, resp.getWriter());
            } else if (parts.length == 2) {
                found = rest.getRec(RecType.CONTR, parts[CMNEM_PART], resp.getWriter());
            }
        } else if ("user".equals(parts[TYPE_PART])) {
            if (parts.length == 1) {
                found = rest.getRec(RecType.USER, resp.getWriter());
            } else if (parts.length == 2) {
                found = rest.getRec(RecType.USER, parts[CMNEM_PART], resp.getWriter());
            }
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

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        assert user != null;

        String email = user.getEmail();
        final Rest rest = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);

        if (parts.length != 1 || !"user".equals(parts[TYPE_PART])) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final JSONParser p = new JSONParser();
        final Request r = new Request();
        try {
            p.parse(req.getReader(), r);
        } catch (final ParseException e) {
            throw new IOException(e);
        }
        int fields = r.getFields();
        if ((fields & Request.EMAIL) != 0) {
            if (!r.getEmail().equals(email) && !userService.isUserAdmin()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            fields &= ~Request.EMAIL;
            email = r.getEmail();
        }
        if (fields != (Request.MNEM | Request.DISPLAY)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!rest.postUser(r.getMnem(), r.getDisplay(), email, resp.getWriter())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}

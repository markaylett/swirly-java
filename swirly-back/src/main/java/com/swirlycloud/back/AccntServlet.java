/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.util.PathUtil.splitPath;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public final class AccntServlet extends HttpServlet {
    private static final int TYPE_PART = 0;
    private static final int CMNEM_PART = 1;
    private static final int SETTL_DATE_PART = 2;
    private static final int ID_PART = 3;

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public final void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, POST, PUT");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        if (user == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }
        final String email = user.getEmail();
        final Rest rest = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);

        boolean found = false;
        if ("order".equals(parts[TYPE_PART])) {
            if (parts.length == 4) {
                found = rest.deleteOrder(resp.getWriter(), email, parts[CMNEM_PART],
                        Integer.parseInt(parts[SETTL_DATE_PART]), Long.parseLong(parts[ID_PART]));
            }
        } else if ("trade".equals(parts[TYPE_PART])) {
            if (parts.length == 4) {
                found = rest.deleteTrade(email, parts[CMNEM_PART],
                        Integer.parseInt(parts[SETTL_DATE_PART]), Long.parseLong(parts[ID_PART]));
                if (found) {
                    resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                    return;
                }
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
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        if (user == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }
        final String email = user.getEmail();
        final Rest rest = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);

        boolean found = false;
        if (parts.length == 0) {
            found = rest.getAccnt(resp.getWriter(), email);
        } else if ("order".equals(parts[TYPE_PART])) {
            if (parts.length == 1) {
                found = rest.getOrder(resp.getWriter(), email);
            } else if (parts.length == 4) {
                found = rest.getOrder(resp.getWriter(), email, parts[CMNEM_PART],
                        Integer.parseInt(parts[SETTL_DATE_PART]), Long.parseLong(parts[ID_PART]));
            }
        } else if ("trade".equals(parts[TYPE_PART])) {
            if (parts.length == 1) {
                found = rest.getTrade(resp.getWriter(), email);
            } else if (parts.length == 4) {
                found = rest.getTrade(resp.getWriter(), email, parts[CMNEM_PART],
                        Integer.parseInt(parts[SETTL_DATE_PART]), Long.parseLong(parts[ID_PART]));
            }
        } else if ("posn".equals(parts[TYPE_PART])) {
            if (parts.length == 1) {
                found = rest.getPosn(resp.getWriter(), email);
            } else if (parts.length == 2) {
                found = rest.getPosn(resp.getWriter(), email, parts[CMNEM_PART]);
            } else if (parts.length == 3) {
                found = rest.getPosn(resp.getWriter(), email, parts[CMNEM_PART],
                        Integer.parseInt(parts[SETTL_DATE_PART]));
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
        if (user == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }
        final String email = user.getEmail();
        final Rest rest = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length != 3 || !"order".equals(parts[TYPE_PART])) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final String cmnem = parts[CMNEM_PART];
        final int settlDate = Integer.parseInt(parts[SETTL_DATE_PART]);

        final JSONParser p = new JSONParser();
        final Request r = new Request();
        try {
            p.parse(req.getReader(), r);
        } catch (final ParseException e) {
            throw new IOException(e);
        }
        if (r.getFields() != (Request.REF | Request.ACTION | Request.TICKS | Request.LOTS | Request.MIN_LOTS)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!rest.postOrder(resp.getWriter(), email, cmnem, settlDate, r.getRef(), r.getAction(), r.getTicks(),
                r.getLots(), r.getMinLots())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        if (user == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }
        final String email = user.getEmail();
        final Rest rest = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length != 4 || !"order".equals(parts[TYPE_PART])) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final String cmnem = parts[CMNEM_PART];
        final int settlDate = Integer.parseInt(parts[SETTL_DATE_PART]);
        final long id = Long.parseLong(parts[ID_PART]);

        final JSONParser p = new JSONParser();
        final Request r = new Request();
        try {
            p.parse(req.getReader(), r);
        } catch (final ParseException e) {
            throw new IOException(e);
        }
        if (r.getFields() != Request.LOTS) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!rest.putOrder(resp.getWriter(), email, cmnem, settlDate, id, r.getLots())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}

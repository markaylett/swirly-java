/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.oak;

import static com.swirlycloud.oak.WebUtil.splitPathInfo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private Map<String, String> userMap = new ConcurrentHashMap<>();

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
        userMap = Context.getRest().newUserMap();
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
        final String umnem = userMap.get(user.getEmail());
        if (umnem == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Rest rest = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if ("order".equals(parts[0])) {
            if (parts.length == 2) {
                rest.deleteOrder(sb, umnem, Integer.parseInt(parts[1]));
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else if ("trade".equals(parts[0])) {
            if (parts.length == 2) {
                rest.deleteTrade(sb, umnem, Integer.parseInt(parts[1]));
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (sb.length() == 0) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
        log(sb.toString());
        resp.getWriter().append(sb);
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
        final String umnem = userMap.get(user.getEmail());
        if (umnem == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Rest rest = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if (parts.length == 0) {
            rest.getAccnt(sb, umnem);
        } else if ("order".equals(parts[0])) {
            if (parts.length == 1) {
                rest.getOrder(sb, umnem);
            } else if (parts.length == 2) {
                if (!rest.getOrder(sb, umnem, Integer.parseInt(parts[1]))) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else if ("trade".equals(parts[0])) {
            if (parts.length == 1) {
                rest.getTrade(sb, umnem);
            } else if (parts.length == 2) {
                if (!rest.getTrade(sb, umnem, Integer.parseInt(parts[1]))) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else if ("posn".equals(parts[0])) {
            if (parts.length == 1) {
                rest.getPosn(sb, umnem);
            } else if (parts.length == 2) {
                rest.getPosn(sb, umnem, parts[1]);
            } else if (parts.length == 3) {
                if (!rest.getPosn(sb, umnem, parts[1], Integer.parseInt(parts[2]))) {
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
        log(sb.toString());
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
        final String umnem = userMap.get(user.getEmail());
        if (umnem == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Rest rest = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if (parts.length != 1 || !"order".equals(parts[0])) {
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
        if (r.getFields() != (Request.CONTR | Request.SETTL_DATE | Request.REF | Request.ACTION
                | Request.TICKS | Request.LOTS | Request.MIN_LOTS)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        rest.postOrder(sb, umnem, r.getContr(), r.getSettlDate(), r.getRef(), r.getAction(),
                r.getTicks(), r.getLots(), r.getMinLots());
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
        log(sb.toString());
        resp.getWriter().append(sb);
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
        final String umnem = userMap.get(user.getEmail());
        if (umnem == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Rest rest = Context.getRest();
        final StringBuilder sb = new StringBuilder();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPathInfo(pathInfo);
        if (parts.length != 2 || !"order".equals(parts[0])) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        final long id = Integer.parseInt(parts[1]);

        final JSONParser p = new JSONParser();
        final Request r = new Request();
        try {
            p.parse(req.getReader(), r);
        } catch (ParseException e) {
            throw new IOException(e);
        }
        if (r.getFields() != Request.LOTS) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        rest.putOrder(sb, umnem, id, r.getLots());
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
        log(sb.toString());
        resp.getWriter().append(sb);
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.engine.Constants.DEPTH;
import static com.swirlycloud.util.PathUtil.splitPath;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public final class MarketServlet extends HttpServlet {

    private static final int CMNEM_PART = 0;
    private static final int SETTL_DATE_PART = 1;

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

        final UserService userService = UserServiceFactory.getUserService();
        if (!userService.isUserLoggedIn()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Rest ctx = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);

        boolean found = false;
        if (parts.length == 0) {
            found = ctx.getMarket(DEPTH, resp.getWriter());
        } else if (parts.length == 1) {
            found = ctx.getMarket(parts[CMNEM_PART], DEPTH, resp.getWriter());
        } else if (parts.length == 2) {
            found = ctx.getMarket(parts[CMNEM_PART], Integer.parseInt(parts[SETTL_DATE_PART]),
                    DEPTH, resp.getWriter());
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
        if (!userService.isUserLoggedIn() || !userService.isUserAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Rest rest = Context.getRest();

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length != 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final String cmnem = parts[CMNEM_PART];

        final JSONParser p = new JSONParser();
        final Request r = new Request();
        try {
            p.parse(req.getReader(), r);
        } catch (final ParseException e) {
            throw new IOException(e);
        }
        if (r.getFields() != (Request.SETTL_DATE | Request.EXPIRY_DATE)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!rest.postMarket(cmnem, r.getSettlDate(), r.getExpiryDate(), resp.getWriter())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}

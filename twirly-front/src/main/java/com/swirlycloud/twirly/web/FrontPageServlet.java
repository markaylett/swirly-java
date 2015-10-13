/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.StringUtil.splitPath;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.rest.Rest;

@SuppressWarnings("serial")
public final class FrontPageServlet extends HttpServlet {

    private static Page getPage(HttpServletRequest req) {
        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length != 1) {
            return null;
        }
        Page page;
        if ("home".equals(parts[0])) {
            page = Page.HOME;
        } else if ("trade".equals(parts[0])) {
            page = Page.TRADE;
        } else if ("contr".equals(parts[0])) {
            page = Page.CONTR;
        } else if ("market".equals(parts[0])) {
            page = Page.MARKET;
        } else if ("trader".equals(parts[0])) {
            page = Page.TRADER;
        } else if ("about".equals(parts[0])) {
            page = Page.ABOUT;
        } else if ("contact".equals(parts[0])) {
            page = Page.CONTACT;
        } else if ("auth".equals(parts[0])) {
            page = Page.AUTH;
        } else if ("error".equals(parts[0])) {
            page = Page.ERROR;
        } else if ("signin".equals(parts[0])) {
            page = Page.SIGNIN;
        } else if ("signout".equals(parts[0])) {
            page = Page.SIGNOUT;
        } else {
            page = null;
        }
        return page;
    }

    protected static Realm realm;
    protected static Rest rest;

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Page page = getPage(req);
        if (page == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "page not found");
            return;
        }
        final PageState state = new PageState(realm, rest, req, resp, page);

        // Expose state to JSP page.
        if (page.isRestricted() && !state.authenticate()) {
            return;
        }
        if (page == Page.TRADE) {
            try {
                if (!state.isUserTrader()) {
                    page = Page.SIGNUP;
                }
            } catch (final ServiceUnavailableException e) {
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
                return;
            }
        } else if (page == Page.SIGNOUT) {
            // Invalidate security token.
            req.logout();
        }
        req.setAttribute("state", state);
        final RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher(page.getJspPage());
        dispatcher.forward(req, resp);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        final Page page = getPage(req);
        if (page != Page.ERROR) {
            super.doPost(req, resp);
            return;
        }
        final PageState state = new PageState(realm, rest, req, resp, page);
        req.setAttribute("state", state);
        final RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher(page.getJspPage());
        dispatcher.forward(req, resp);
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doPut(req, resp);
    }

    public static void setRealm(Realm realm) {
        FrontPageServlet.realm = realm;
    }

    public static void setRest(Rest rest) {
        FrontPageServlet.rest = rest;
    }
}

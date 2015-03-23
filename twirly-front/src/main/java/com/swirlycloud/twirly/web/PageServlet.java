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

@SuppressWarnings("serial")
public final class PageServlet extends HttpServlet {

    private static final ThreadLocal<PageState> stateTls = new ThreadLocal<PageState>() {
        @Override
        protected final PageState initialValue() {
            return new PageState(realm);
        }
    };

    protected static Realm realm;

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length != 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
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
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Expose state to JSP page.
        final PageState state = stateTls.get();
        state.setState(req, resp, page);
        if (page.isRestricted()) {
            if (!state.isUserLoggedIn()) {
                resp.sendRedirect(state.getLoginUrl());
                return;
            }
        }
        if (page == Page.TRADE && !state.isUserTrader()) {
            page = Page.SIGNUP;
        }
        req.setAttribute("state", state);
        final RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
                page.getJspPage());
        dispatcher.forward(req, resp);
    }

    public static void setRealm(Realm realm) {
        PageServlet.realm = realm;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import static com.swirlycloud.swirly.util.StringUtil.splitPath;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.rest.Rest;

@SuppressWarnings("serial")
public final class FrontPageServlet extends HttpServlet {

    private static Page getPage(HttpServletRequest req) {
        final String pathInfo = req.getPathInfo();
        final String[] parts = splitPath(pathInfo);
        if (parts.length != 1) {
            return null;
        }
        Page page = null;
        final String name = parts[0];
        switch (name.charAt(0)) {
        case 'a':
            if ("about".equals(name)) {
                page = Page.ABOUT;
            } else if ("auth".equals(name)) {
                page = Page.AUTH;
            }
            break;
        case 'c':
            if ("contact".equals(name)) {
                page = Page.CONTACT;
            } else if ("contr".equals(name)) {
                page = Page.CONTR;
            }
            break;
        case 'e':
            if ("error".equals(name)) {
                page = Page.ERROR;
            }
            break;
        case 'h':
            if ("home".equals(name)) {
                page = Page.HOME;
            }
            break;
        case 'm':
            if ("market".equals(name)) {
                page = Page.MARKET;
            }
            break;
        case 'o':
            if ("order".equals(name)) {
                page = Page.ORDER;
            }
            break;
        case 'q':
            if ("quote".equals(name)) {
                page = Page.QUOTE;
            }
            break;
        case 's':
            if ("signin".equals(name)) {
                page = Page.SIGNIN;
            } else if ("signout".equals(name)) {
                page = Page.SIGNOUT;
            }
            break;
        case 't':
            if ("trader".equals(name)) {
                page = Page.TRADER;
            }
            break;
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
        if (page == Page.ORDER || page == Page.QUOTE) {
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

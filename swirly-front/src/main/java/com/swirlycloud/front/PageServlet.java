/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.front;

import static com.swirlycloud.util.PathUtil.splitPath;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public final class PageServlet extends HttpServlet {

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

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
//        } else if ("user".equals(parts[0])) {
//            page = Page.USER;
        } else if ("about".equals(parts[0])) {
            page = Page.ABOUT;
        } else if ("contact".equals(parts[0])) {
            page = Page.CONTACT;
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Expose state to JSP page.
        final PageState state = new PageState(page);
        if (page.isRestricted()) {
            if (!state.isUserLoggedIn()) {
                resp.sendRedirect(state.getLoginURL());
                return;
            }
            if (!state.isUserRegistered()) {
                page = Page.SIGNUP;
            }
        }
        req.setAttribute("state", state);
        final RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
                page.getJspPage());
        dispatcher.forward(req, resp);
    }
}

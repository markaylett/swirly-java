/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class CatalinaRealm implements Realm {

    @Override
    public final String getLoginUrl(HttpServletResponse resp, String targetUrl) {
        return resp.encodeURL("login.jsp");
    }

    @Override
    public final String getLogoutUrl(HttpServletResponse resp, String targetUrl) {
        return resp.encodeURL("logout.jsp");
    }

    @Override
    public final String getUserEmail(HttpServletRequest req) {
        final Principal p = req.getUserPrincipal();
        return p != null ? p.getName() : null;
    }

    @Override
    public final boolean isDevServer(HttpServletRequest req) {
        return false;
    }

    @Override
    public final boolean isUserLoggedIn(HttpServletRequest req) {
        return req.getUserPrincipal() != null;
    }

    @Override
    public final boolean isUserAdmin(HttpServletRequest req) {
        return isUserLoggedIn(req) && req.isUserInRole("admin");
    }

    @Override
    public final boolean isUserTrader(HttpServletRequest req) {
        return isUserLoggedIn(req) && req.isUserInRole("trader");
    }
}

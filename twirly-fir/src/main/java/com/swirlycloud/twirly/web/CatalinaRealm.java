/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class CatalinaRealm implements Realm {

    @Override
    public final boolean authenticate(HttpServletRequest req, HttpServletResponse resp,
            String targetUrl) throws IOException, ServletException {
        return req.authenticate(resp);
    }

    @Override
    public final String getSignInUrl(HttpServletResponse resp, String targetUrl) {
        return resp.encodeURL("/page/auth");
    }

    @Override
    public final String getSignOutUrl(HttpServletResponse resp, String targetUrl) {
        return resp.encodeURL("/page/signout");
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
    public final boolean isUserSignedIn(HttpServletRequest req) {
        return req.getUserPrincipal() != null;
    }

    @Override
    public final boolean isUserAdmin(HttpServletRequest req) {
        return isUserSignedIn(req) && req.isUserInRole("admin");
    }

    @SuppressWarnings("deprecation")
    @Override
    public final boolean isUserTrader(HttpServletRequest req) {
        return isUserSignedIn(req) && req.isUserInRole("trader");
    }
}

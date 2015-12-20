/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

public final class AppEngineRealm implements Realm {
    private final UserService userService;

    public AppEngineRealm() {
        userService = UserServiceFactory.getUserService();
    }

    @Override
    public final boolean authenticate(HttpServletRequest req, HttpServletResponse resp,
            String targetUrl) throws IOException {
        if (isUserSignedIn(req)) {
            return true;
        }
        resp.sendRedirect(getSignInUrl(resp, targetUrl));
        return false;
    }

    @Override
    public final String getSignInUrl(HttpServletResponse resp, String targetUrl) {
        return userService.createLoginURL(targetUrl);
    }

    @Override
    public final String getSignOutUrl(HttpServletResponse resp, String targetUrl) {
        return userService.createLogoutURL(targetUrl);
    }

    @Override
    public final String getUserEmail(HttpServletRequest req) {
        final User user = userService.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    @Override
    public final boolean isDevServer(HttpServletRequest req) {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    @Override
    public final boolean isUserSignedIn(HttpServletRequest req) {
        return userService.isUserLoggedIn();
    }

    @Override
    public final boolean isUserAdmin(HttpServletRequest req) {
        return isUserSignedIn(req) && userService.isUserAdmin();
    }
}

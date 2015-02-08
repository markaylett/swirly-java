/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

public final class GaeContext implements ServletContextListener, Context {
    private static final class Holder {
        private static final Rest rest = new Rest(new GaeModel());
        private static final UserService userService = UserServiceFactory.getUserService();

        private static void init() {
            // Force static initialisation.
        }
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        Holder.init();
        RestServlet.setContext(this);
    }

    @Override
    public final void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
    }

    @Override
    public final Rest getRest() {
        return Holder.rest;
    }

    @Override
    public final String getUserEmail() {
        final User user = Holder.userService.getCurrentUser();
        assert user != null;
        return user.getEmail();
    }

    @Override
    public final boolean isDevEnv() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    @Override
    public final boolean isUserLoggedIn() {
        return Holder.userService.isUserLoggedIn();
    }

    @Override
    public final boolean isUserAdmin() {
        return Holder.userService.isUserAdmin();
    }
}

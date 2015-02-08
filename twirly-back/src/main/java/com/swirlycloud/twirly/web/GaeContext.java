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

public final class GaeContext implements ServletContextListener {
    private static final class Holder {
        private static final Rest rest = new Rest(new GaeModel());
        private static final UserService userService = UserServiceFactory.getUserService();

        private static void init() {
            // Force static initialisation.
        }
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        Holder.init();
    }

    @Override
    public final void contextDestroyed(ServletContextEvent event) {
    }

    public static Rest getRest() {
        return Holder.rest;
    }

    public static boolean isDevEnv() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    public static boolean isUserLoggedIn() {
        return Holder.userService.isUserLoggedIn();
    }

    public static boolean isUserAdmin() {
        return Holder.userService.isUserAdmin();
    }

    public static String getUserEmail() {
        final User user = Holder.userService.getCurrentUser();
        assert user != null;
        return user.getEmail();
    }
}

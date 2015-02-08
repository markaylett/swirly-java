/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.appengine.api.utils.SystemProperty;

public final class Context implements ServletContextListener {
    private static final class Holder {
        private static final Rest rest = new Rest(new DatastoreModel());

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
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
}

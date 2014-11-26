/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.oak;

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

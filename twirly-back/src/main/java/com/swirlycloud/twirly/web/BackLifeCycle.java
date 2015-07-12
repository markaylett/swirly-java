/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.swirlycloud.twirly.io.AsyncDatastore;
import com.swirlycloud.twirly.io.AsyncDatastoreService;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.AppEngineDatastore;
import com.swirlycloud.twirly.io.JdbcDatastore;
import com.swirlycloud.twirly.mock.MockDatastore;

public final class BackLifeCycle implements ServletContextListener {
    private AutoCloseable closeable;

    private final void close(final ServletContext sc) {
        try {
            // We have to check for null here because contextDestroyed may be called even when
            // contextInitialized fails.
            if (closeable != null) {
                closeable.close();
                closeable = null;
            }
        } catch (Exception e) {
            sc.log("failed to close datastore", e);
        }
    }

    private static Datastore getDatastore(ServletContext sc) {
        Datastore datastore;
        final String url = sc.getInitParameter("url");
        if (url == null || url.equals("appengine:datastore:")) {
            // Default.
            datastore = new AppEngineDatastore();
        } else if (url.startsWith("jdbc:mysql:")) {
            final String user = sc.getInitParameter("user");
            final String password = sc.getInitParameter("password");
            // Locate, load, and link the MySql Jdbc driver.
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("mysql jdbc driver not found", e);
            }
            datastore = new JdbcDatastore(url, user, password);
        } else if (url.equals("mock:")) {
            datastore = new MockDatastore();
        } else {
            throw new RuntimeException("invalid datastore url: " + url);
        }
        return datastore;
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        final Datastore datastore = getDatastore(sc);
        this.closeable = datastore;
        boolean success = false;
        try {
            if (sc.getServerInfo().startsWith("Apache Tomcat")) {
                RestServlet.setRealm(new CatalinaRealm());
                final AsyncDatastore asyncDatastore = new AsyncDatastoreService(datastore);
                this.closeable = asyncDatastore;
                try {
                    RestServlet.setDatastore(asyncDatastore);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("failed to create async datastore", e);
                }
            } else {
                RestServlet.setRealm(new AppEngineRealm());
                RestServlet.setDatastore(datastore);
            }
            success = true;
        } finally {
            if (!success) {
                close(sc);
            }
        }
    }

    @Override
    public final void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
        final ServletContext sc = event.getServletContext();
        close(sc);
    }
}

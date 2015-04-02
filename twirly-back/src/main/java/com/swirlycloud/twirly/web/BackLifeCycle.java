/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.swirlycloud.twirly.io.AsyncModel;
import com.swirlycloud.twirly.io.AsyncModelService;
import com.swirlycloud.twirly.io.DatastoreModel;
import com.swirlycloud.twirly.io.JdbcModel;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.mock.MockModel;

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
            sc.log("failed to close model", e);
        }
    }

    private static Model getModel(ServletContext sc) {
        Model model;
        final String url = sc.getInitParameter("url");
        if (url == null || url.equals("datastore:")) {
            // Default.
            model = new DatastoreModel();
        } else if (url.startsWith("jdbc:mysql:")) {
            final String user = sc.getInitParameter("user");
            final String password = sc.getInitParameter("password");
            // Locate, load, and link the MySql Jdbc driver.
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("mysql jdbc driver not found", e);
            }
            model = new JdbcModel(url, user, password);
        } else if (url.equals("mock:")) {
            model = new MockModel();
        } else {
            throw new RuntimeException("invalid model url: " + url);
        }
        return model;
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        final Model model = getModel(sc);
        this.closeable = model;
        boolean success = false;
        try {
            if (sc.getServerInfo().startsWith("Apache Tomcat")) {
                RestServlet.setRealm(new CatalinaRealm());
                final AsyncModel asyncModel = new AsyncModelService(model);
                this.closeable = asyncModel;
                try {
                    RestServlet.setModel(asyncModel);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("failed to create async model", e);
                }
            } else {
                RestServlet.setRealm(new AppEngineRealm());
                RestServlet.setModel(model);
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

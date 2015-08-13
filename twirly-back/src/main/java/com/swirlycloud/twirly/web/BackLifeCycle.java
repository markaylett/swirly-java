/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.app.LockableServ;
import com.swirlycloud.twirly.app.Serv;
import com.swirlycloud.twirly.domain.BasicFactory;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.io.AppEngineDatastore;
import com.swirlycloud.twirly.io.AsyncDatastore;
import com.swirlycloud.twirly.io.AsyncDatastoreService;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.JdbcDatastore;
import com.swirlycloud.twirly.rest.BackRest;
import com.swirlycloud.twirly.rest.Rest;

public final class BackLifeCycle implements ServletContextListener {

    private static final @NonNull Factory FACTORY = new BasicFactory();

    private Serv serv;

    private final void close(final ServletContext sc) {
        try {
            // We have to check for null here because contextDestroyed may be called even when
            // contextInitialized fails.
            if (serv != null) {
                serv.close();
                serv = null;
            }
        } catch (final Exception e) {
            sc.log("failed to close serv", e);
        }
    }

    private static @NonNull Datastore getDatastore(ServletContext sc) {
        Datastore datastore;
        final String url = sc.getInitParameter("url");
        if (url == null || url.equals("appengine:datastore:")) {
            // Default.
            datastore = new AppEngineDatastore(FACTORY);
        } else if (url.startsWith("jdbc:mysql:")) {
            final String user = sc.getInitParameter("user");
            final String password = sc.getInitParameter("password");
            // Locate, load, and link the MySql Jdbc driver.
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("mysql jdbc driver not found", e);
            }
            datastore = new JdbcDatastore(url, user, password, FACTORY);
        } else {
            throw new RuntimeException("invalid datastore url: " + url);
        }
        return datastore;
    }

    @SuppressWarnings("resource")
    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        final Datastore datastore = getDatastore(sc);
        AutoCloseable resource = datastore;
        boolean success = false;
        try {
            final long now = now();
            Realm realm = null;
            LockableServ serv = null;
            if (sc.getServerInfo().startsWith("Apache Tomcat")) {
                realm = new CatalinaRealm();
                final AsyncDatastore asyncDatastore = new AsyncDatastoreService(datastore);
                // AsyncDatastore owns Datastore.
                resource = asyncDatastore;
                try {
                    serv = new LockableServ(asyncDatastore, FACTORY, now);
                    // LockableServ owns AsyncDatastore.
                    resource = serv;
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("failed to create async datastore", e);
                }
            } else {
                realm = new AppEngineRealm();
                serv = new LockableServ(datastore, FACTORY, now);
                // LockableServ owns Datastore.
                resource = serv;
            }
            final Rest rest = new BackRest(serv);
            // Commit.
            RestServlet.setRealm(realm);
            RestServlet.setRest(rest);
            this.serv = serv;
            success = true;
        } finally {
            if (!success) {
                try {
                    resource.close();
                } catch (final Exception e) {
                    sc.log("failed to close resource", e);
                }
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

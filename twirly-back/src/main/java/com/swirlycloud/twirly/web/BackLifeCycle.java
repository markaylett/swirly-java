/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.ServFactory;
import com.swirlycloud.twirly.exception.UncheckedIOException;
import com.swirlycloud.twirly.io.AppEngineCache;
import com.swirlycloud.twirly.io.AppEngineDatastore;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.JdbcDatastore;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.io.SpyCache;
import com.swirlycloud.twirly.rest.BackRest;
import com.swirlycloud.twirly.rest.Rest;

public final class BackLifeCycle implements ServletContextListener {

    private Model model;

    private static @NonNull Realm newAppEngineRealm(ServletContext sc, Factory factory) {
        return new AppEngineRealm();
    }

    private static @NonNull Realm newCatalinaRealm(ServletContext sc, Factory factory) {
        return new CatalinaRealm();
    }

    private static @NonNull Datastore newAppEngineDatastore(ServletContext sc, Factory factory) {
        return new AppEngineDatastore(factory);
    }

    private static @NonNull Datastore newCatalinaDatastore(ServletContext sc, Factory factory) {
        final String url = sc.getInitParameter("url");
        final String user = sc.getInitParameter("user");
        final String password = sc.getInitParameter("password");
        if (url.startsWith("jdbc:mysql:")) {
            // Locate, load, and link the Mysql Jdbc driver.
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("mysql jdbc driver not found", e);
            }
        }
        return new JdbcDatastore(url, user, password, factory);
    }

    @SuppressWarnings("unused")
    private static @NonNull Cache newAppEngineCache(ServletContext sc, Factory factory) {
        return new AppEngineCache();
    }

    @SuppressWarnings("unused")
    private static @NonNull Cache newCatalinaCache(ServletContext sc, Factory factory) {
        try {
            return new SpyCache(new InetSocketAddress("localhost", 11211));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final void close(final ServletContext sc) {
        try {
            // We have to check for null here because contextDestroyed may be called even when
            // contextInitialized fails.
            if (model != null) {
                model.close();
                model = null;
            }
        } catch (final Exception e) {
            sc.log("failed to close model", e);
        }
    }

    @SuppressWarnings({ "resource", "null" })
    private final void open(ServletContext sc, @NonNull Factory factory)
            throws InterruptedException {

        Realm realm = null;
        Datastore datastore = null;
        Model model = null;
        Cache cache = null;
        Rest rest = null;
        final ServletContainer c = ServletContainer.valueOf(sc);
        try {
            if (c == ServletContainer.APP_ENGINE) {
                realm = newAppEngineRealm(sc, factory);
                datastore = newAppEngineDatastore(sc, factory);
                model = datastore;
                //cache = newAppEngineCache(sc, factory);
            } else if (c == ServletContainer.CATALINA) {
                realm = newCatalinaRealm(sc, factory);
                datastore = newCatalinaDatastore(sc, factory);
                model = datastore;
                //cache = newCatalinaCache(sc, factory);
            } else {
                throw new RuntimeException("unsupported servlet container");
            }
            //model = new CacheModel(datastore, cache);
            // Model now owns cache.
            cache = null;
            rest = new BackRest(model, datastore, factory, now());
        } finally {
            if (rest == null) {
                if (cache != null) {
                    try {
                        cache.close();
                    } catch (final Exception e) {
                        sc.log("failed to close cache", e);
                    }
                }
                if (model != null) {
                    try {
                        model.close();
                    } catch (final Exception e) {
                        sc.log("failed to close model", e);
                    }
                }
            }
        }
        // Commit.
        this.model = model;
        RestServlet.setRealm(realm);
        RestServlet.setRest(rest);
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        try {
            open(sc, new ServFactory());
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            sc.log("service interrupted", e);
        }
    }

    @Override
    public final void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
        final ServletContext sc = event.getServletContext();
        close(sc);
    }
}

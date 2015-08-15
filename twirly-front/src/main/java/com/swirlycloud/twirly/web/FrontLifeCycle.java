/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.io.CacheUtil.NO_CACHE;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.BasicFactory;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.exception.UncheckedIOException;
import com.swirlycloud.twirly.io.AppEngineCache;
import com.swirlycloud.twirly.io.AppEngineModel;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.CacheModel;
import com.swirlycloud.twirly.io.JdbcModel;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.io.SpyCache;
import com.swirlycloud.twirly.rest.FrontRest;
import com.swirlycloud.twirly.rest.Rest;

public final class FrontLifeCycle implements ServletContextListener {

    private static final Factory FACTORY = new BasicFactory();

    private Model model;

    private final void close(final ServletContext sc) {
        try {
            // We have to check for null here because contextDestroyed may be called even when
            // contextInitialized fails.
            if (model != null) {
                model.close();
                model = null;
            }
        } catch (Exception e) {
            sc.log("failed to close model", e);
        }
    }

    @SuppressWarnings("resource")
    private static @NonNull Model newAppEngineModel(ServletContext sc) {
        Model model = new AppEngineModel(FACTORY);
        Cache cache = null;
        boolean success = false;
        try {
            cache = new AppEngineCache();
            model = new CacheModel(model, cache);
            success = true;
        } finally {
            if (!success) {
                if (cache != null) {
                    try {
                        cache.close();
                    } catch (Exception e) {
                        sc.log("failed to close cache", e);
                    }
                }
                try {
                    model.close();
                } catch (Exception e) {
                    sc.log("failed to close model", e);
                }
            }
        }
        return model;
    }

    @SuppressWarnings("resource")
    private static @NonNull Model newJdbcModel(ServletContext sc, String url, String user,
            String password) throws IOException {
        Model model = new JdbcModel(url, user, password, FACTORY);
        Cache cache = null;
        boolean success = false;
        try {
            cache = new SpyCache(new InetSocketAddress("localhost", 11211));
            model = new CacheModel(model, cache);
            success = true;
        } finally {
            if (!success) {
                if (cache != null) {
                    try {
                        cache.close();
                    } catch (Exception e) {
                        sc.log("failed to close cache", e);
                    }
                }
                try {
                    model.close();
                } catch (Exception e) {
                    sc.log("failed to close model", e);
                }
            }
        }
        return model;
    }

    @SuppressWarnings("resource")
    private static @NonNull Model getModel(ServletContext sc) {
        Model model;
        final String url = sc.getInitParameter("url");
        if (url == null || url.equals("appengine:datastore:")) {
            // Default.
            model = newAppEngineModel(sc);
        } else if (url.startsWith("jdbc:mysql:")) {
            final String user = sc.getInitParameter("user");
            final String password = sc.getInitParameter("password");
            // Locate, load, and link the MySql Jdbc driver.
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("mysql jdbc driver not found", e);
            }
            try {
                model = newJdbcModel(sc, url, user, password);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new RuntimeException("invalid datastore url: " + url);
        }
        return model;
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        final Model model = getModel(sc);
        boolean success = false;
        try {
            Realm realm = null;
            if (sc.getServerInfo().startsWith("Apache Tomcat")) {
                realm = new CatalinaRealm();
            } else {
                realm = new AppEngineRealm();
            }
            final Rest rest = new FrontRest(model, NO_CACHE);
            // Commit.
            PageServlet.setRealm(realm);
            RestServlet.setRealm(realm);
            RestServlet.setRest(rest);
            this.model = model;
            success = true;
        } finally {
            if (!success) {
                try {
                    model.close();
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

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

    private Model model;

    private static @NonNull Realm newAppEngineRealm(ServletContext sc, Factory factory) {
        return new AppEngineRealm();
    }

    private static @NonNull Realm newCatalinaRealm(ServletContext sc, Factory factory) {
        return new CatalinaRealm();
    }

    private static @NonNull Model newAppEngineModel(ServletContext sc, Factory factory) {
        return new AppEngineModel(factory);
    }

    private static @NonNull Model newCatalinaModel(ServletContext sc, Factory factory) {
        final String url = sc.getInitParameter("url");
        final String user = sc.getInitParameter("user");
        final String password = sc.getInitParameter("password");
        if (url.startsWith("jdbc:mysql:")) {
            // Locate, load, and link the Mysql Jdbc driver.
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException("mysql jdbc driver not found", e);
            }
        }
        return new JdbcModel(url, user, password, factory);
    }

    private static @NonNull Cache newAppEngineCache(ServletContext sc, Factory factory) {
        return new AppEngineCache();
    }

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

    @SuppressWarnings("resource")
    private final void open(ServletContext sc, @NonNull Factory factory) {

        Realm realm = null;
        Model model = null;
        Cache cache = null;
        Rest rest = null;
        final ServletContainer c = ServletContainer.valueOf(sc);
        try {
            if (c == ServletContainer.APP_ENGINE) {
                realm = newAppEngineRealm(sc, factory);
                model = newAppEngineModel(sc, factory);
                cache = newAppEngineCache(sc, factory);
            } else if (c == ServletContainer.CATALINA) {
                realm = newCatalinaRealm(sc, factory);
                model = newCatalinaModel(sc, factory);
                cache = newCatalinaCache(sc, factory);
            } else {
                throw new RuntimeException("unsupported servlet container");
            }
            model = new CacheModel(model, cache);
            // Model now owns cache.
            cache = null;
            rest = new FrontRest(model);
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
        FrontPageServlet.setRealm(realm);
        FrontPageServlet.setRest(rest);
        RestServlet.setRealm(realm);
        RestServlet.setRest(rest);
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        open(sc, new BasicFactory());
    }

    @Override
    public final void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
        final ServletContext sc = event.getServletContext();
        close(sc);
    }
}

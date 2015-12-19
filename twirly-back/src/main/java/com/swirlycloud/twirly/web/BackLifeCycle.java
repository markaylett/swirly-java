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

import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.AppEngineCache;
import com.swirlycloud.twirly.io.AppEngineDatastore;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.JdbcDatastore;
import com.swirlycloud.twirly.io.SpyCache;
import com.swirlycloud.twirly.rest.BackRest;
import com.swirlycloud.twirly.rest.Rest;
import com.swirlycloud.twirly.unchecked.UncheckedIOException;

public final class BackLifeCycle implements ServletContextListener {

    private Datastore datastore;
    private Cache cache;

    private static @NonNull Realm newAppEngineRealm(ServletContext sc) {
        return new AppEngineRealm();
    }

    private static @NonNull Realm newCatalinaRealm(ServletContext sc) {
        return new CatalinaRealm();
    }

    private static @NonNull Datastore newAppEngineDatastore(ServletContext sc) {
        return new AppEngineDatastore();
    }

    private static @NonNull Datastore newCatalinaDatastore(ServletContext sc) {
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
        return new JdbcDatastore(url, user, password);
    }

    private static @NonNull Cache newAppEngineCache(ServletContext sc) {
        return new AppEngineCache();
    }

    private static @NonNull Cache newCatalinaCache(ServletContext sc) {
        try {
            return new SpyCache(new InetSocketAddress("localhost", 11211));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final void close(final ServletContext sc) {
        // We have to check for null here because contextDestroyed may be called even when
        // contextInitialized fails.
        try {
            if (cache != null) {
                try {
                    cache.close();
                    cache = null;
                } catch (final Exception e) {
                    sc.log("failed to close cache", e);
                }
            }
        } finally {
            if (datastore != null) {
                try {
                    datastore.close();
                    datastore = null;
                } catch (final Exception e) {
                    sc.log("failed to close datastore", e);
                }
            }
        }
    }

    @SuppressWarnings("resource")
    private final void open(ServletContext sc)
            throws NotFoundException, ServiceUnavailableException, InterruptedException {

        Realm realm = null;
        Datastore datastore = null;
        Cache cache = null;
        Rest rest = null;
        final ServletContainer c = ServletContainer.valueOf(sc);
        try {
            if (c == ServletContainer.APP_ENGINE) {
                realm = newAppEngineRealm(sc);
                datastore = newAppEngineDatastore(sc);
                cache = newAppEngineCache(sc);
            } else if (c == ServletContainer.CATALINA) {
                realm = newCatalinaRealm(sc);
                datastore = newCatalinaDatastore(sc);
                cache = newCatalinaCache(sc);
            } else {
                throw new RuntimeException("unsupported servlet container");
            }
            rest = new BackRest(datastore, cache, now());
        } finally {
            if (rest == null) {
                try {
                    if (cache != null) {
                        try {
                            cache.close();
                        } catch (final Exception e) {
                            sc.log("failed to close cache", e);
                        }
                    }
                } finally {
                    if (datastore != null) {
                        try {
                            datastore.close();
                        } catch (final Exception e) {
                            sc.log("failed to close datastore", e);
                        }
                    }
                }
            }
        }
        // Commit.
        this.datastore = datastore;
        this.cache = cache;
        RestServlet.setRealm(realm);
        RestServlet.setRest(rest);
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        try {
            open(sc);
        } catch (final ServException e) {
            sc.log("internal error", e);
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

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

public final class AppEngineRealm implements Realm {
    private final UserService userService;
    // Cache of all traders.
    private final Map<String, Boolean> traderCache = new ConcurrentHashMap<>();

    public AppEngineRealm() {
        userService = UserServiceFactory.getUserService();
    }

    @Override
    public final boolean authenticate(HttpServletRequest req, HttpServletResponse resp,
            String targetUrl) throws IOException {
        if (isUserSignedIn(req)) {
            return true;
        }
        resp.sendRedirect(getSignInUrl(resp, targetUrl));
        return false;
    }

    @Override
    public final String getSignInUrl(HttpServletResponse resp, String targetUrl) {
        return userService.createLoginURL(targetUrl);
    }

    @Override
    public final String getSignOutUrl(HttpServletResponse resp, String targetUrl) {
        return userService.createLogoutURL(targetUrl);
    }

    @Override
    public final String getUserEmail(HttpServletRequest req) {
        final User user = userService.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    @Override
    public final boolean isDevServer(HttpServletRequest req) {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    @Override
    public final boolean isUserSignedIn(HttpServletRequest req) {
        return userService.isUserLoggedIn();
    }

    @Override
    public final boolean isUserAdmin(HttpServletRequest req) {
        return isUserSignedIn(req) && userService.isUserAdmin();
    }

    @SuppressWarnings("deprecation")
    @Override
    public final boolean isUserTrader(HttpServletRequest req) {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return false;
        }
        Boolean cached = traderCache.get(user.getEmail());
        if (cached == null) {
            final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            final Filter filter = new FilterPredicate("email", FilterOperator.EQUAL,
                    user.getEmail());
            final Query q = new Query("Trader").setFilter(filter).setKeysOnly();
            final PreparedQuery pq = datastore.prepare(q);
            final int traderCount = pq.countEntities(FetchOptions.Builder.withLimit(1));
            if (traderCount == 1) {
                // Assumption: once a trader, always a trader.
                cached = Boolean.TRUE;
                traderCache.put(user.getEmail(), cached);
            } else {
                cached = Boolean.FALSE;
            }
        }
        return cached.booleanValue();
    }
}

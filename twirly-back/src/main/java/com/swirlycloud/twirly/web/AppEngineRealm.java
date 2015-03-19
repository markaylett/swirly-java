/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import com.swirlycloud.twirly.web.Realm;

public final class AppEngineRealm implements Realm {
    private final UserService userService;
    // Cache of all traders.
    private final Map<String, Boolean> traderCache = new ConcurrentHashMap<>();

    public AppEngineRealm() {
        userService = UserServiceFactory.getUserService();
    }

    @Override
    public final String getUserEmail() {
        final User user = userService.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    @Override
    public final String getLoginUrl(String targetUrl) {
        return userService.createLoginURL(targetUrl);
    }

    @Override
    public final String getLogoutUrl(String targetUrl) {
        return userService.createLogoutURL(targetUrl);
    }

    @Override
    public final boolean isDevEnv() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    @Override
    public final boolean isUserLoggedIn() {
        return userService.isUserLoggedIn();
    }

    @Override
    public final boolean isUserAdmin() {
        return isUserLoggedIn() && userService.isUserAdmin();
    }

    @Override
    public final boolean isUserTrader() {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return false;
        }
        Boolean cached = traderCache.get(user.getEmail());
        if (cached == null) {
            final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            final Filter filter = new FilterPredicate("email", FilterOperator.EQUAL, user.getEmail());
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

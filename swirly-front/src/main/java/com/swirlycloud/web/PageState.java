/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.web;

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

public final class PageState {
    private final Page page;
    private final UserService userService;
    private final User user;
    private int traderCount = -1;

    public PageState(Page page) {
        this.page = page;
        userService = UserServiceFactory.getUserService();
        user = userService.getCurrentUser();
    }

    public final boolean isHomePage() {
        return page == Page.HOME;
    }

    public final boolean isTradePage() {
        return page == Page.TRADE;
    }

    public final boolean isContrPage() {
        return page == Page.CONTR;
    }

    public final boolean isAdminPage() {
        return page == Page.MARKET || page == Page.TRADER;
    }

    public final boolean isMarketPage() {
        return page == Page.MARKET;
    }

    public final boolean isTraderPage() {
        return page == Page.TRADER;
    }

    public final boolean isAboutPage() {
        return page == Page.ABOUT;
    }

    public final boolean isContactPage() {
        return page == Page.CONTACT;
    }

    public final boolean isUserLoggedIn() {
        return user != null;
    }

    public final boolean isUserAdmin() {
        return isUserLoggedIn() && userService.isUserAdmin();
    }

    public final boolean isTrader() {
        if (!isUserLoggedIn()) {
            return false;
        }
        if (traderCount < 0) {
            // Lazy.
            final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            final Filter filter = new FilterPredicate("email", FilterOperator.EQUAL,
                    user.getEmail());
            final Query q = new Query("Trader").setFilter(filter).setKeysOnly();
            final PreparedQuery pq = datastore.prepare(q);
            traderCount = pq.countEntities(FetchOptions.Builder.withLimit(1));
        }
        return traderCount == 1;
    }

    public final String getUserName() {
        return user.getEmail();
    }

    public final String getLoginURL() {
        return userService.createLoginURL(page.getPath());
    }

    public final String getLogoutURL() {
        return userService.createLogoutURL(Page.HOME.getPath());
    }
}

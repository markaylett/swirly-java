/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.front;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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
    private int userCount = -1;

    public PageState(Page page) {
        this.page = page;
        userService = UserServiceFactory.getUserService();
        user = userService.getCurrentUser();
    }

    public final boolean isHomePage() {
        return page == Page.HOME;
    }

    public final boolean isTraderPage() {
        return page == Page.TRADER;
    }

    public final boolean isContrPage() {
        return page == Page.CONTR;
    }

    public final boolean isUserPage() {
        return page == Page.USER;
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

    public final boolean isUserRegistered() {
        if (!isUserLoggedIn()) {
            return false;
        }
        if (userCount < 0) {
            // Lazy.
            final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            final Key key = KeyFactory.createKey("UserEmail", user.getEmail());
            final Filter filter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                    FilterOperator.EQUAL, key);
            final Query q = new Query("UserEmail").setFilter(filter).setKeysOnly();
            final PreparedQuery pq = datastore.prepare(q);
            userCount = pq.countEntities(FetchOptions.Builder.withLimit(1));
        }
        return userCount == 1;
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

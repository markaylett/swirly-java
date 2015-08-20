/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class PageState {
    private final Realm realm;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final Page page;

    public PageState(Realm realm, HttpServletRequest req, HttpServletResponse resp, Page page) {
        this.realm = realm;
        this.req = req;
        this.resp = resp;
        this.page = page;
    }

    public final boolean authenticate() throws IOException, ServletException {
        return realm.authenticate(req, resp, page.getPath());
    }

    public final String getSignInUrl() {
        final Page target = page.isInternal() ? Page.HOME : page;
        return realm.getSignInUrl(resp, target.getPath());
    }

    public final String getSignOutUrl() {
        final Page target = page.isInternal() || page.isRestricted() ? Page.HOME : page;
        return realm.getSignOutUrl(resp, target.getPath());
    }

    public final String getUserEmail() {
        return realm.getUserEmail(req);
    }

    public final boolean isDevServer() {
        return realm.isDevServer(req);
    }

    public final boolean isUserLoggedIn() {
        return realm.isUserSignedIn(req);
    }

    public final boolean isUserAdmin() {
        return realm.isUserAdmin(req);
    }

    public final boolean isUserTrader() {
        return realm.isUserTrader(req);
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

    public final boolean isErrorPage() {
        return page == Page.ERROR;
    }

    public final boolean isLoginPage() {
        return page == Page.SIGNIN;
    }

    public final boolean isSignUpPage() {
        return page == Page.SIGNUP;
    }
}

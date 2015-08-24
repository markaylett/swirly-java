/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.rest.Rest;

public final class PageState {
    private static final int UNKNOWN = 0;
    private static final int YES = 1;
    private static final int NO = 2;

    private final Realm realm;
    private final Rest rest;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final Page page;
    // UNKNOWN, YES or NO.
    private int isUserTrader = UNKNOWN;

    public PageState(Realm realm, Rest rest, HttpServletRequest req, HttpServletResponse resp,
            Page page) {
        this.realm = realm;
        this.rest = rest;
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

    /**
     * @return true if trader attribute is a non-empty mnemonic.
     * @throws IOException
     * @throws ServiceUnavailableException
     */
    public final boolean isUserTrader() throws ServiceUnavailableException, IOException {
        if (isUserTrader == UNKNOWN) {
            final String email = realm.getUserEmail(req);
            if (email != null) {
                final String trader = rest.findTraderByEmail(email);
                isUserTrader = trader != null ? YES : NO;
            } else {
                isUserTrader = NO;
            }
        }
        return isUserTrader == YES;
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

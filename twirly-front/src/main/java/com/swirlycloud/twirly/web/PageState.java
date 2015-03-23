/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class PageState {
    private final Realm realm;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private Page page;

    public PageState(Realm realm) {
        this.realm = realm;
    }

    public final void setState(HttpServletRequest req, HttpServletResponse resp, Page page) {
        this.req = req;
        this.resp = resp;
        this.page = page;
    }

    public final String getLoginUrl() {
        return realm.getLoginUrl(resp, page.getPath());
    }

    public final String getLogoutUrl() {
        return realm.getLogoutUrl(resp, Page.HOME.getPath());
    }

    public final String getUserEmail() {
        return realm.getUserEmail(req);
    }

    public final boolean isDevServer() {
        return realm.isDevServer(req);
    }

    public final boolean isUserLoggedIn() {
        return realm.isUserLoggedIn(req);
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
}

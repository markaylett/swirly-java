/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

public final class PageState implements Realm {
    private final Realm realm;
    private Page page;

    public PageState(Realm realm) {
        this.realm = realm;
    }

    @Override
    public final String getUserEmail() {
        return realm.getUserEmail();
    }

    @Override
    public final String getLoginUrl(String targetUrl) {
        return realm.getLoginUrl(targetUrl);
    }

    @Override
    public final String getLogoutUrl(String targetUrl) {
        return realm.getLogoutUrl(targetUrl);
    }

    @Override
    public final boolean isDevEnv() {
        return realm.isDevEnv();
    }

    @Override
    public final boolean isUserLoggedIn() {
        return realm.isUserLoggedIn();
    }

    @Override
    public final boolean isUserAdmin() {
        return realm.isUserAdmin();
    }

    @Override
    public final boolean isUserTrader() {
        return realm.isUserTrader();
    }

    public final void setPage(Page page) {
        this.page = page;
    }

    public final String getLoginURL() {
        return getLoginUrl(page.getPath());
    }

    public final String getLogoutURL() {
        return getLoginUrl(Page.HOME.getPath());
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

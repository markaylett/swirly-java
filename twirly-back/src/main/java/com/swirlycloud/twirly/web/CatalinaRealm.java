/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import com.swirlycloud.twirly.web.Realm;

public final class CatalinaRealm implements Realm {

    @Override
    public final String getUserEmail() {
        return "mark.aylett@gmail.com";
    }

    @Override
    public final String getLoginUrl(String targetUrl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String getLogoutUrl(String targetUrl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean isDevEnv() {
        return true;
    }

    @Override
    public final boolean isUserLoggedIn() {
        return true;
    }

    @Override
    public final boolean isUserAdmin() {
        return true;
    }

    @Override
    public final boolean isUserTrader() {
        throw new UnsupportedOperationException();
    }
}

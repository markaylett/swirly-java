/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import com.swirlycloud.twirly.app.Model;

public final class TcContext implements Context {
    private final Rest rest;

    public TcContext(Model model) {
        rest = new Rest(model);
    }

    @Override
    public final Rest getRest() {
        return rest;
    }

    @Override
    public final String getUserEmail() {
        return "mark.aylett@gmail.com";
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
}

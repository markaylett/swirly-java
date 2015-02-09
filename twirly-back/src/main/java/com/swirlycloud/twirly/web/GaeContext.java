/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

public final class GaeContext implements Context {
    private final Rest rest = new Rest(new GaeModel());
    private final UserService userService = UserServiceFactory.getUserService();

    @Override
    public final Rest getRest() {
        return rest;
    }

    @Override
    public final String getUserEmail() {
        final User user = userService.getCurrentUser();
        assert user != null;
        return user.getEmail();
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
        return userService.isUserAdmin();
    }
}

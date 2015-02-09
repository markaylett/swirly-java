/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

public interface Context {

    Rest getRest();

    String getUserEmail();

    boolean isDevEnv();

    boolean isUserLoggedIn();

    boolean isUserAdmin();
}

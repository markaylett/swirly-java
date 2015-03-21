/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

public interface Realm {

    String getUserEmail();

    String getLoginUrl(String targetUrl);

    String getLogoutUrl(String targetUrl);

    boolean isDevEnv();

    boolean isUserLoggedIn();

    boolean isUserAdmin();
    
    boolean isUserTrader();
}

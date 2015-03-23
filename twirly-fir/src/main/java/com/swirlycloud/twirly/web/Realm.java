/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Realm {

    String getLoginUrl(HttpServletResponse resp, String targetUrl);

    String getLogoutUrl(HttpServletResponse resp, String targetUrl);

    String getUserEmail(HttpServletRequest req);

    boolean isDevServer(HttpServletRequest req);

    boolean isUserLoggedIn(HttpServletRequest req);

    boolean isUserAdmin(HttpServletRequest req);
    
    boolean isUserTrader(HttpServletRequest req);
}

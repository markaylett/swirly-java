/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Realm {

    // Return false if authentication is incomplete.

    boolean authenticate(HttpServletRequest req, HttpServletResponse resp, String targetUrl)
            throws IOException, ServletException;

    String getSignInUrl(HttpServletResponse resp, String targetUrl);

    String getSignOutUrl(HttpServletResponse resp, String targetUrl);

    String getUserEmail(HttpServletRequest req);

    boolean isDevServer(HttpServletRequest req);

    boolean isUserSignedIn(HttpServletRequest req);

    boolean isUserAdmin(HttpServletRequest req);

    boolean isUserTrader(HttpServletRequest req);
}

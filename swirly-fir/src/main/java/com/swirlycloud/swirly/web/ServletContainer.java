/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import javax.servlet.ServletContext;

public enum ServletContainer {
    /**
     * Google App Engine.
     */
    APP_ENGINE,
    /**
     * Catalina is Tomcat's servlet container.
     */
    CATALINA;

    /**
     * @param sc
     *            Servlet context.
     * @return Servlet container.
     * @throws IllegalArgumentException
     *             if {@code sc} is invalid.
     */
    public static ServletContainer valueOf(ServletContext sc) {
        ServletContainer val;
        final String si = sc.getServerInfo();
        if (si.startsWith("Apache Tomcat")) {
            val = CATALINA;
        } else {
            // Default.
            val = APP_ENGINE;
        }
        return val;
    }
}

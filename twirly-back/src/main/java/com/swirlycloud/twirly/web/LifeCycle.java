/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.swirlycloud.twirly.app.Model;
import com.swirlycloud.twirly.concurrent.AsyncModelService;
import com.swirlycloud.twirly.mock.MockModel;

public final class LifeCycle implements ServletContextListener {
    private Model model;

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request was invoked.
        final ServletContext sc = event.getServletContext();
        final String url = sc.getInitParameter("url");
        if (url == null || url.equals("datastore:")) {
            // Default.
            model = new GaeModel();
        } else if (url.equals("mock:")) {
            model = new MockModel();
        } else {
            throw new RuntimeException("invalid model url: " + url);
        }
        if (sc.getServerInfo().startsWith("Apache Tomcat")) {
            try {
                RestServlet.setContext(new TcContext(new AsyncModelService(model)));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("failed to create async model", e);
            }
        } else {
            RestServlet.setContext(new GaeContext(model));
        }
    }

    @Override
    public final void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
        try {
            if (model != null) {
                model.close();
            }
        } catch (Exception e) {
            final ServletContext sc = event.getServletContext();
            sc.log("failed to close model", e);
        }
    }
}

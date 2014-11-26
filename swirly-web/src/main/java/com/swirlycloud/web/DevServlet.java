/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public final class DevServlet extends HttpServlet {

    private final URL getModuleUrl(HttpServletRequest req, String module) throws IOException {
        final ModulesService service = ModulesServiceFactory.getModulesService();
        final String host = service.getVersionHostname("backend", service.getCurrentVersion());
        final StringBuilder sb = new StringBuilder();
        sb.append(req.getScheme());
        sb.append("://");
        sb.append(host);
        sb.append(req.getRequestURI());
        final String qs = req.getQueryString();
        if (qs != null) {
            sb.append('?');
            sb.append(qs);
        }
        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new IOException(e);
        }
    }

    private final void setCredentials(HttpServletRequest in, HTTPRequest out) {
        final Cookie[] cookies = in.getCookies();
        for (int i = 0; i < cookies.length; ++i) {
            final String name = cookies[i].getName();
            if ("dev_appserver_login".equals(name)) {
                out.setHeader(new HTTPHeader("Cookie", name + "=" + cookies[i].getValue()));
            }
        }
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final URL url = getModuleUrl(req, "backend");
        final HTTPRequest backend = new HTTPRequest(url, HTTPMethod.GET);
        setCredentials(req, backend);
        resp.getOutputStream().write(
                URLFetchServiceFactory.getURLFetchService().fetch(backend).getContent());
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) {
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) {
    }
}

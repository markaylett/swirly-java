/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.front;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
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
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * Alternative to dispatch.xml for testing.
 * <p>
 * The development server does not currently support dispatch.xml routing. This Servlet forwards
 * requests from the default module to the backend module when using the development server.
 * </p>
 *
 * @author Mark Aylett
 * */
@SuppressWarnings("serial")
public final class ProxyServlet extends HttpServlet {
    private String module;

    /**
     * Generate equivalent URL for target module.
     *
     * @param req
     *            The original request.
     * @param module
     *            The target module.
     * @return the new URL.
     * @throws IOException
     */
    private static URL getModuleUrl(HttpServletRequest req, String module) throws IOException {
        final ModulesService service = ModulesServiceFactory.getModulesService();
        final String host = service.getVersionHostname(module, service.getCurrentVersion());
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
        } catch (final MalformedURLException e) {
            throw new IOException(e);
        }
    }

    private static void setCredentials(HttpServletRequest req, HTTPRequest modReq) {
        final Cookie[] cookies = req.getCookies();
        for (int i = 0; i < cookies.length; ++i) {
            final String name = cookies[i].getName();
            // The development server passes credentials in the dev_appserver_login cookie.
            if ("dev_appserver_login".equals(name)) {
                modReq.setHeader(new HTTPHeader("Cookie", name + "=" + cookies[i].getValue()));
            }
        }
    }

    private static void setPayload(HttpServletRequest req, HTTPRequest modReq) throws IOException {
        int len = req.getContentLength();
        if (len <= 0) {
            return;
        }
        final byte[] arr = new byte[len];
        try (final InputStream is = req.getInputStream()) {
            int off = 0;
            do {
                final int n = is.read(arr, off, len);
                if (n < 0) {
                    break;
                }
                len -= n;
                off += n;
            } while (len > 0);
        }
        modReq.setPayload(arr);
    }

    private static void proxy(HttpServletRequest req, HttpServletResponse resp, HTTPMethod method,
            String module) throws IOException {
        // Prepare module request.
        final URL url = getModuleUrl(req, module);
        final HTTPRequest modReq = new HTTPRequest(url, method);
        setCredentials(req, modReq);
        setPayload(req, modReq);

        // Send client request to module.
        final HTTPResponse modResp = URLFetchServiceFactory.getURLFetchService().fetch(modReq);

        // Send module response to client.
        for (final HTTPHeader h : modResp.getHeaders()) {
            resp.setHeader(h.getName(), h.getValue());
        }
        resp.setStatus(modResp.getResponseCode());
        if (modResp.getContent() != null) {
            resp.getOutputStream().write(modResp.getContent());
        }
    }

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
        module = config.getInitParameter("module");
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        proxy(req, resp, HTTPMethod.DELETE, module);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        proxy(req, resp, HTTPMethod.GET, module);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        proxy(req, resp, HTTPMethod.POST, module);
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        proxy(req, resp, HTTPMethod.PUT, module);
    }
}

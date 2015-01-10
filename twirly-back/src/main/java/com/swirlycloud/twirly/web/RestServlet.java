/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public abstract class RestServlet extends HttpServlet {

    protected Params newParams(final HttpServletRequest req) {
        return new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                final String s = req.getParameter(name);
                final Object val;
                if (s != null) {
                    if ("depth".equals(name)) {
                        val = Integer.valueOf(s);
                    } else if ("expired".equals(name) || "internal".equals(name)) {
                        val = Boolean.valueOf(s);
                    } else {
                        val = s;
                    }
                } else {
                    val = null;
                }
                return (T) val;
            }
        };
    }

    protected void sendJsonResponse(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    protected void sendJsonResponse(HttpServletResponse resp, ServException e) throws IOException {
        e.toJson(null, resp.getWriter());
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(e.getNum());
    }

    protected boolean isDevEnv() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    @Override
    public final void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, POST, PUT");
            resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
            resp.setHeader("Access-Control-Max-Age", "86400");
        }
    }
}
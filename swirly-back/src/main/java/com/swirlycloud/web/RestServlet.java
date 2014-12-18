/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;
import com.swirlycloud.exception.ServException;
import com.swirlycloud.function.UnaryFunction;

@SuppressWarnings("serial")
public abstract class RestServlet extends HttpServlet {

    protected UnaryFunction<String, String> newParams(final HttpServletRequest req) {
        return new UnaryFunction<String, String>() {
            @Override
            public final String call(String arg) {
                return req.getParameter(arg);
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

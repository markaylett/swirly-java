/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

import static com.swirlycloud.swirly.util.JsonUtil.parseStartObject;

import java.io.IOException;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.ServException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.exception.TraderNotFoundException;
import com.swirlycloud.swirly.rest.Rest;
import com.swirlycloud.swirly.rest.RestRequest;
import com.swirlycloud.swirly.util.Params;

@SuppressWarnings("serial")
public abstract class RestServlet extends HttpServlet {

    private static boolean isBoolean(@NonNull String name) {
        boolean result = false;
        switch (name.charAt(0)) {
        case 'e':
            result = "expired".equals(name);
            break;
        case 'i':
            result = "internal".equals(name);
            break;
        case 'q':
            result = "quotes".equals(name);
            break;
        case 'v':
            result = "views".equals(name);
            break;
        }
        return result;
    }

    protected static Realm realm;
    protected static Rest rest;

    protected final Params newParams(final HttpServletRequest req) {
        return new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                assert name != null;
                final String s = req.getParameter(name);
                final Object val;
                if (s != null) {
                    if ("depth".equals(name)) {
                        val = Integer.valueOf(s);
                    } else if (isBoolean(name)) {
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

    protected final RestRequest parseRequest(HttpServletRequest req) throws BadRequestException {
        try (final JsonParser p = Json.createParser(req.getReader())) {
            parseStartObject(p);
            final RestRequest r = new RestRequest();
            r.parse(p);
            return r;
        } catch (IllegalArgumentException | IOException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    protected final void sendJsonResponse(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Swirly-Timeout", String.valueOf(rest.getTimeout()));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @SuppressWarnings("null")
    protected final void sendJsonResponse(HttpServletResponse resp, ServException e)
            throws IOException {
        e.toJson(null, resp.getWriter());
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setStatus(e.getHttpStatus());
    }

    protected final void setNoContent(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Swirly-Timeout", String.valueOf(rest.getTimeout()));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    protected final @NonNull String getTrader(HttpServletRequest req)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final String email = realm.getUserEmail(req);
        assert email != null;
        final String trader = rest.findTraderByEmail(email);
        if (trader == null) {
            throw new TraderNotFoundException(String.format("trader '%s' does not exist", email));
        }
        return trader;
    }

    @Override
    public final void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, POST, PUT");
            resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
            resp.setHeader("Access-Control-Max-Age", "86400");
        }
    }

    public static void setRealm(Realm realm) {
        RestServlet.realm = realm;
    }

    public static void setRest(Rest rest) {
        RestServlet.rest = rest;
    }
}

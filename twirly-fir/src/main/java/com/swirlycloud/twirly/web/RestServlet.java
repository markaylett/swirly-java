/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;

import java.io.IOException;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.exception.TraderNotFoundException;
import com.swirlycloud.twirly.rest.Request;
import com.swirlycloud.twirly.rest.Rest;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public abstract class RestServlet extends HttpServlet {

    protected static Realm realm;
    protected static Rest rest;

    protected final Params newParams(final HttpServletRequest req) {
        return new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                final String s = req.getParameter(name);
                final Object val;
                if (s != null) {
                    if ("depth".equals(name)) {
                        val = Integer.valueOf(s);
                    } else if ("expired".equals(name) || "internal".equals(name)
                            || "views".equals(name)) {
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

    protected final Request parseRequest(HttpServletRequest req) throws BadRequestException {
        try (final JsonParser p = Json.createParser(req.getReader())) {
            parseStartObject(p);
            final Request r = new Request();
            r.parse(p);
            return r;
        } catch (IllegalArgumentException | IOException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    protected final void sendJsonResponse(HttpServletResponse resp, long timeout) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Twirly-Timeout", String.valueOf(timeout));
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

    protected final void setNoContent(HttpServletResponse resp, long timeout) {
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Twirly-Timeout", String.valueOf(timeout));
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

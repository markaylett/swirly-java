/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.StringUtil.splitPath;

import java.io.IOException;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.ForbiddenException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.function.UnaryFunction;

@SuppressWarnings("serial")
public final class RecServlet extends RestServlet {

    private static final int TYPE_PART = 0;
    private static final int CMNEM_PART = 1;

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            final UserService userService = UserServiceFactory.getUserService();
            if (!userService.isUserLoggedIn()) {
                throw new UnauthorizedException("user is not logged-in");
            }

            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final UnaryFunction<String, String> params = newParams(req);

            boolean match = false;
            if (parts.length == 0) {
                rest.getRec(userService.isUserAdmin(), params, resp.getWriter());
                match = true;
            } else if ("asset".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getRec(RecType.ASSET, params, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getRec(RecType.ASSET, parts[CMNEM_PART], params, resp.getWriter());
                    match = true;
                }
            } else if ("contr".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getRec(RecType.CONTR, params, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getRec(RecType.CONTR, parts[CMNEM_PART], params, resp.getWriter());
                    match = true;
                }
            } else if ("trader".equals(parts[TYPE_PART])) {
                if (!userService.isUserAdmin()) {
                    throw new BadRequestException("user is not an admin");
                }
                if (parts.length == 1) {
                    rest.getRec(RecType.TRADER, params, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getRec(RecType.TRADER, parts[CMNEM_PART], params, resp.getWriter());
                    match = true;
                }
            }

            if (!match) {
                throw new NotFoundException("resource does not exist");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (isDevEnv()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            final UserService userService = UserServiceFactory.getUserService();
            if (!userService.isUserLoggedIn()) {
                throw new UnauthorizedException("user is not logged-in");
            }
            final User user = userService.getCurrentUser();
            assert user != null;

            String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 1 || !"trader".equals(parts[TYPE_PART])) {
                throw new MethodNotAllowedException("post is not allowed on this resource");
            }

            final Request r = new Request();
            try (JsonParser p = Json.createParser(req.getReader())) {
                r.parse(p);
            }
            int fields = r.getFields();
            if ((fields & Request.EMAIL) != 0) {
                if (!r.getEmail().equals(email) && !userService.isUserAdmin()) {
                    throw new ForbiddenException("user is not an admin");
                }
                fields &= ~Request.EMAIL;
                email = r.getEmail();
            }
            if (fields != (Request.MNEM | Request.DISPLAY)) {
                throw new BadRequestException("request fields are invalid");
            }
            rest.postTrader(r.getMnem(), r.getDisplay(), email, resp.getWriter());
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

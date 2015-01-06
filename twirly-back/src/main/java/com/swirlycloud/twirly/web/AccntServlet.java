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
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public final class AccntServlet extends RestServlet {
    private static final int TYPE_PART = 0;
    private static final int CMNEM_PART = 1;
    private static final int SETTL_DATE_PART = 2;
    private static final int ID_PART = 3;

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            boolean match = false;
            if (parts.length > 0) {
                if ("order".equals(parts[TYPE_PART])) {
                    if (parts.length == 4) {
                        rest.deleteOrder(email, parts[CMNEM_PART],
                                Integer.parseInt(parts[SETTL_DATE_PART]),
                                Long.parseLong(parts[ID_PART]));
                        match = true;
                    }
                } else if ("trade".equals(parts[TYPE_PART])) {
                    if (parts.length == 4) {
                        rest.deleteTrade(email, parts[CMNEM_PART],
                                Integer.parseInt(parts[SETTL_DATE_PART]),
                                Long.parseLong(parts[ID_PART]));
                        match = true;
                    }
                }
            }

            if (!match) {
                throw new NotFoundException("resource does not exist");
            }
            resp.setHeader("Cache-Control", "no-cache");
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
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
            final User user = userService.getCurrentUser();
            assert user != null;

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final Params params = newParams(req);
            final long now = System.currentTimeMillis();

            boolean match = false;
            if (parts.length == 0) {
                rest.getAccnt(email, params, now, resp.getWriter());
                match = true;
            } else if ("order".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getOrder(email, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getOrder(email, parts[CMNEM_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getOrder(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 4) {
                    rest.getOrder(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]),
                            Long.parseLong(parts[ID_PART]), params, now, resp.getWriter());
                    match = true;
                }
            } else if ("trade".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getTrade(email, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getTrade(email, parts[CMNEM_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getTrade(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 4) {
                    rest.getTrade(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]),
                            Long.parseLong(parts[ID_PART]), params, now, resp.getWriter());
                    match = true;
                }
            } else if ("posn".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getPosn(email, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getPosn(email, parts[CMNEM_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getPosn(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), params, now, resp.getWriter());
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

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 3 || !"order".equals(parts[TYPE_PART])) {
                throw new MethodNotAllowedException("post is not allowed on this resource");
            }
            final String cmnem = parts[CMNEM_PART];
            final int settlDate = Integer.parseInt(parts[SETTL_DATE_PART]);

            final Request r = new Request();
            try (JsonParser p = Json.createParser(req.getReader())) {
                r.parse(p);
            }
            if (r.getFields() != (Request.REF | Request.ACTION | Request.TICKS | Request.LOTS | Request.MIN_LOTS)) {
                throw new BadRequestException("request fields are invalid");
            }
            final long now = System.currentTimeMillis();
            rest.postOrder(email, cmnem, settlDate, r.getRef(), r.getAction(), r.getTicks(),
                    r.getLots(), r.getMinLots(), now, resp.getWriter());
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 4 || !"order".equals(parts[TYPE_PART])) {
                throw new MethodNotAllowedException("put is not allowed on this resource");
            }
            final String cmnem = parts[CMNEM_PART];
            final int settlDate = Integer.parseInt(parts[SETTL_DATE_PART]);
            final long id = Long.parseLong(parts[ID_PART]);

            final Request r = new Request();
            try (JsonParser p = Json.createParser(req.getReader())) {
                r.parse(p);
            }
            if (r.getFields() != Request.LOTS) {
                throw new BadRequestException("request fields are invalid");
            }
            final long now = System.currentTimeMillis();
            rest.putOrder(email, cmnem, settlDate, id, r.getLots(), now, resp.getWriter());
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

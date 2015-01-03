/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.StringUtil.splitPath;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.function.UnaryFunction;

@SuppressWarnings("serial")
public final class MarketServlet extends RestServlet {

    private static final int CMNEM_PART = 0;
    private static final int SETTL_DATE_PART = 1;

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
                rest.getMarket(params, resp.getWriter());
                match = true;
            } else if (parts.length == 1) {
                rest.getMarket(parts[CMNEM_PART], params, resp.getWriter());
                match = true;
            } else if (parts.length == 2) {
                rest.getMarket(parts[CMNEM_PART], Integer.parseInt(parts[SETTL_DATE_PART]), params,
                        resp.getWriter());
                match = true;
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

            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 1) {
                throw new MethodNotAllowedException("post is not allowed on this resource");
            }
            final String cmnem = parts[CMNEM_PART];

            final JSONParser p = new JSONParser();
            final Request r = new Request();
            try {
                p.parse(req.getReader(), r);
            } catch (final ParseException e) {
                throw new BadRequestException("request could not be parsed");
            }
            if (r.getFields() != (Request.SETTL_DATE | Request.FIXING_DATE | Request.EXPIRY_DATE)) {
                throw new BadRequestException("request fields are invalid");
            }
            rest.postMarket(cmnem, r.getSettlDate(), r.getFixingDate(), r.getExpiryDate(),
                    resp.getWriter());
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

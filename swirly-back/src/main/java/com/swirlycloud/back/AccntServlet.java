/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.util.PathUtil.splitPath;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.swirlycloud.exception.BadRequestException;
import com.swirlycloud.exception.NotFoundException;
import com.swirlycloud.exception.ServException;
import com.swirlycloud.exception.UnauthorizedException;

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
                throw new UnauthorizedException("Not logged-in");
            }
            final User user = userService.getCurrentUser();
            assert user != null;

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            boolean found = false;
            if ("order".equals(parts[TYPE_PART])) {
                if (parts.length == 4) {
                    found = rest.deleteOrder(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]),
                            Long.parseLong(parts[ID_PART]));
                }
            } else if ("trade".equals(parts[TYPE_PART])) {
                if (parts.length == 4) {
                    found = rest.deleteTrade(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]),
                            Long.parseLong(parts[ID_PART]));
                }
            }

            if (!found) {
                throw new NotFoundException("Not found");
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
                throw new UnauthorizedException("Not logged-in");
            }
            final User user = userService.getCurrentUser();
            assert user != null;

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            boolean found = false;
            if (parts.length == 0) {
                found = rest.getAccnt(email, resp.getWriter());
            } else if ("order".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    found = rest.getOrder(email, resp.getWriter());
                } else if (parts.length == 2) {
                    found = rest.getOrder(email, parts[CMNEM_PART], resp.getWriter());
                } else if (parts.length == 3) {
                    found = rest.getOrder(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), resp.getWriter());
                } else if (parts.length == 4) {
                    found = rest.getOrder(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]),
                            Long.parseLong(parts[ID_PART]), resp.getWriter());
                }
            } else if ("trade".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    found = rest.getTrade(email, resp.getWriter());
                } else if (parts.length == 2) {
                    found = rest.getTrade(email, parts[CMNEM_PART], resp.getWriter());
                } else if (parts.length == 3) {
                    found = rest.getTrade(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), resp.getWriter());
                } else if (parts.length == 4) {
                    found = rest.getTrade(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]),
                            Long.parseLong(parts[ID_PART]), resp.getWriter());
                }
            } else if ("posn".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    found = rest.getPosn(email, resp.getWriter());
                } else if (parts.length == 2) {
                    found = rest.getPosn(email, parts[CMNEM_PART], resp.getWriter());
                } else if (parts.length == 3) {
                    found = rest.getPosn(email, parts[CMNEM_PART],
                            Integer.parseInt(parts[SETTL_DATE_PART]), resp.getWriter());
                }
            }

            if (!found) {
                throw new NotFoundException("Not found");
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
                throw new UnauthorizedException("Not logged-in");
            }
            final User user = userService.getCurrentUser();
            assert user != null;

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            if (parts.length != 3 || !"order".equals(parts[TYPE_PART])) {
                throw new NotFoundException("Not found");
            }
            final String cmnem = parts[CMNEM_PART];
            final int settlDate = Integer.parseInt(parts[SETTL_DATE_PART]);

            final JSONParser p = new JSONParser();
            final Request r = new Request();
            try {
                p.parse(req.getReader(), r);
            } catch (final ParseException e) {
                throw new IOException(e);
            }
            if (r.getFields() != (Request.REF | Request.ACTION | Request.TICKS | Request.LOTS | Request.MIN_LOTS)) {
                throw new BadRequestException("Invalid json fields");
            }
            if (!rest.postOrder(email, cmnem, settlDate, r.getRef(), r.getAction(), r.getTicks(),
                    r.getLots(), r.getMinLots(), resp.getWriter())) {
                throw new NotFoundException("Not found");
            }
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
                throw new UnauthorizedException("Not logged-in");
            }
            final User user = userService.getCurrentUser();
            assert user != null;

            final String email = user.getEmail();
            final Rest rest = Context.getRest();

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            if (parts.length != 4 || !"order".equals(parts[TYPE_PART])) {
                throw new NotFoundException("Not found");
            }
            final String cmnem = parts[CMNEM_PART];
            final int settlDate = Integer.parseInt(parts[SETTL_DATE_PART]);
            final long id = Long.parseLong(parts[ID_PART]);

            final JSONParser p = new JSONParser();
            final Request r = new Request();
            try {
                p.parse(req.getReader(), r);
            } catch (final ParseException e) {
                throw new IOException(e);
            }
            if (r.getFields() != Request.LOTS) {
                throw new BadRequestException("Invalid json fields");
            }
            if (!rest.putOrder(email, cmnem, settlDate, id, r.getLots(), resp.getWriter())) {
                throw new NotFoundException("Not found");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

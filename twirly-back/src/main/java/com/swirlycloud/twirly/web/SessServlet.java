/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.twirly.util.StringUtil.splitPath;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.MethodNotAllowedException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServException;
import com.swirlycloud.twirly.exception.UnauthorizedException;
import com.swirlycloud.twirly.util.Params;

@SuppressWarnings("serial")
public final class SessServlet extends RestServlet {
    private static final int TYPE_PART = 0;
    private static final int MARKET_PART = 1;
    private static final int ID_PART = 2;
    private static final int CONTR_PART = 1;
    private static final int SETTL_DATE_PART = 2;

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserSignedIn(req)) {
                throw new UnauthorizedException("user is not logged-in");
            }
            final String email = realm.getUserEmail(req);

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final long now = System.currentTimeMillis();

            boolean match = false;
            if (parts.length > 0) {
                if ("order".equals(parts[TYPE_PART])) {
                    if (parts.length == 3) {
                        rest.deleteOrder(email, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                                now);
                        match = true;
                    }
                } else if ("trade".equals(parts[TYPE_PART])) {
                    if (parts.length == 3) {
                        rest.deleteTrade(email, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                                now);
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
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserSignedIn(req)) {
                throw new UnauthorizedException("user is not logged-in");
            }
            final String email = realm.getUserEmail(req);

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);
            final Params params = newParams(req);
            final long now = System.currentTimeMillis();

            boolean match = false;
            if (parts.length == 0) {
                rest.getSess(email, params, now, resp.getWriter());
                match = true;
            } else if ("order".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getOrder(email, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getOrder(email, parts[MARKET_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getOrder(email, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                            params, now, resp.getWriter());
                    match = true;
                }
            } else if ("trade".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getTrade(email, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getTrade(email, parts[MARKET_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getTrade(email, parts[MARKET_PART], Long.parseLong(parts[ID_PART]),
                            params, now, resp.getWriter());
                    match = true;
                }
            } else if ("posn".equals(parts[TYPE_PART])) {
                if (parts.length == 1) {
                    rest.getPosn(email, params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 2) {
                    rest.getPosn(email, parts[CONTR_PART], params, now, resp.getWriter());
                    match = true;
                } else if (parts.length == 3) {
                    rest.getPosn(email, parts[CONTR_PART],
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
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserSignedIn(req)) {
                throw new UnauthorizedException("user is not logged-in");
            }
            final String email = realm.getUserEmail(req);

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 2) {
                throw new MethodNotAllowedException("post is not allowed on this resource");
            }
            final String market = parts[MARKET_PART];
            final long now = System.currentTimeMillis();

            final Request r = parseRequest(req);
            if ("order".equals(parts[TYPE_PART])) {
                if (r.getFields() != (Request.REF | Request.ACTION | Request.TICKS //
                        | Request.LOTS | Request.MIN_LOTS)) {
                    throw new BadRequestException("request fields are invalid");
                }
                rest.postOrder(email, market, r.getRef(), r.getAction(), r.getTicks(), r.getLots(),
                        r.getMinLots(), PARAMS_NONE, now, resp.getWriter());
            } else if ("trade".equals(parts[TYPE_PART])) {

                if (!realm.isUserAdmin(req)) {
                    throw new BadRequestException("user is not an admin");
                }
                if (r.getFields() != (Request.TRADER | Request.REF | Request.ACTION //
                        | Request.TICKS | Request.LOTS | Request.ROLE | Request.CPTY)) {
                    throw new BadRequestException("request fields are invalid");
                }
                rest.postTrade(r.getTrader(), market, r.getRef(), r.getAction(), r.getTicks(),
                        r.getLots(), r.getRole(), r.getCpty(), PARAMS_NONE, now, resp.getWriter());
            } else {
                throw new NotFoundException("resource does not exist");
            }
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (realm.isDevServer(req)) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        try {
            if (!realm.isUserSignedIn(req)) {
                throw new UnauthorizedException("user is not logged-in");
            }
            final String email = realm.getUserEmail(req);

            final String pathInfo = req.getPathInfo();
            final String[] parts = splitPath(pathInfo);

            if (parts.length != 3 || !"order".equals(parts[TYPE_PART])) {
                throw new MethodNotAllowedException("put is not allowed on this resource");
            }
            final String market = parts[MARKET_PART];
            final long id = Long.parseLong(parts[ID_PART]);

            final Request r = parseRequest(req);
            if (r.getFields() != Request.LOTS) {
                throw new BadRequestException("request fields are invalid");
            }
            final long now = System.currentTimeMillis();
            rest.putOrder(email, market, id, r.getLots(), PARAMS_NONE, now, resp.getWriter());
            sendJsonResponse(resp);
        } catch (final ServException e) {
            sendJsonResponse(resp, e);
        }
    }
}

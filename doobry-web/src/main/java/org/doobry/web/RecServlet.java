/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.doobry.web.WebUtil.writeError;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.doobry.domain.RecType;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class RecServlet extends HttpServlet {
    private RecType recType;

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
        recType = RecType.valueOf(config.getInitParameter("recType"));
    }

    @Override
    public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final UserService userService = UserServiceFactory.getUserService();
        final User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }

        final Future<CharSequence> fsb = Ctx.getInstance().getRec(recType);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        try {
            resp.getWriter().append(fsb.get());
        } catch (InterruptedException | ExecutionException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp.getWriter(), e.getLocalizedMessage());
        }
    }
}

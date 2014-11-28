/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.front;

public enum Page {
    HOME("/page/home", "/WEB-INF/jsp/home.jsp", false), //
    TRADER("/page/trader", "/WEB-INF/jsp/trader.jsp", true), //
    CONTR("/page/contr", "/WEB-INF/jsp/contr.jsp", true), //
    USER("/page/user", "/WEB-INF/jsp/user.jsp", true), //
    ABOUT("/page/about", "/WEB-INF/jsp/about.jsp", false), //
    CONTACT("/page/contact", "/WEB-INF/jsp/contact.jsp", false), //
    SIGNUP("/page/signup", "/WEB-INF/jsp/signup.jsp", false);

    private final String path;
    private final String jspPage;
    private final boolean restricted;

    private Page(String path, String jspPage, boolean restricted) {
        this.path = path;
        this.jspPage = jspPage;
        this.restricted = restricted;
    }

    public final String getPath() {
        return path;
    }

    public final String getJspPage() {
        return jspPage;
    }

    public final boolean isRestricted() {
        return restricted;
    }
}

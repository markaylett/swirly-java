/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.front;

public enum Page {
    HOME("/page/home", "/WEB-INF/jsp/home.jsp"), //
    TRADER("/page/trader", "/WEB-INF/jsp/trader.jsp"), //
    CONTR("/page/contr", "/WEB-INF/jsp/contr.jsp"), //
    USER("/page/user", "/WEB-INF/jsp/user.jsp"), //
    ABOUT("/page/about", "/WEB-INF/jsp/about.jsp"), //
    CONTACT("/page/contact", "/WEB-INF/jsp/contact.jsp"), //
    SIGNUP("/page/signup", "/WEB-INF/jsp/signup.jsp");

    private final String path;
    private final String jspPage;

    private Page(String path, String jspPage) {
        this.path = path;
        this.jspPage = jspPage;
    }

    public final String getPath() {
        return path;
    }

    public final String getJspPage() {
        return jspPage;
    }
}

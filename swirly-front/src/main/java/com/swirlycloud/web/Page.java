/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.web;

public enum Page {
    HOME("/page/home", "/WEB-INF/jsp/home.jsp", false), //
    TRADE("/page/trade", "/WEB-INF/jsp/trade.jsp", true), //
    CONTR("/page/contr", "/WEB-INF/jsp/contr.jsp", true), //
    MARKET("/page/market", "/WEB-INF/jsp/market.jsp", true), //
    TRADER("/page/trader", "/WEB-INF/jsp/trader.jsp", true), //
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

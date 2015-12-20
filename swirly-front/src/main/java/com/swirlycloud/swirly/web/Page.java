/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.web;

public enum Page {
    HOME("/page/home", "/WEB-INF/jsp/home.jsp", false, false), //
    ORDER("/page/order", "/WEB-INF/jsp/order.jsp", false, true), //
    QUOTE("/page/quote", "/WEB-INF/jsp/quote.jsp", false, true), //
    CONTR("/page/contr", "/WEB-INF/jsp/contr.jsp", false, true), //
    MARKET("/page/market", "/WEB-INF/jsp/market.jsp", false, true), //
    TRADER("/page/trader", "/WEB-INF/jsp/trader.jsp", false, true), //
    ABOUT("/page/about", "/WEB-INF/jsp/about.jsp", false, false), //
    CONTACT("/page/contact", "/WEB-INF/jsp/contact.jsp", false, false), //
    // Internal.
    AUTH("/page/auth", "/WEB-INF/jsp/home.jsp", true, false), //
    // The signin.jsp is used to display signin errors.
    ERROR("/page/error", "/WEB-INF/jsp/signin.jsp", true, false), //
    SIGNIN("/page/signin", "/WEB-INF/jsp/signin.jsp", true, false), //
    SIGNOUT("/page/signout", "/WEB-INF/jsp/home.jsp", true, false), //
    SIGNUP("/page/signup", "/WEB-INF/jsp/signup.jsp", true, false);

    private final String path;
    private final String jspPage;
    private final boolean internal;
    private final boolean restricted;

    private Page(String path, String jspPage, boolean internal, boolean restricted) {
        this.path = path;
        this.jspPage = jspPage;
        this.internal = internal;
        this.restricted = restricted;
    }

    public final String getPath() {
        return path;
    }

    public final String getJspPage() {
        return jspPage;
    }

    public final boolean isInternal() {
        return internal;
    }

    public final boolean isRestricted() {
        return restricted;
    }
}

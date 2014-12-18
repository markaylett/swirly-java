/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import java.io.IOException;
import java.util.Map;

import com.swirlycloud.util.StringUtil;

public final class Trader extends Rec {
    // Internals.
    // Singly-linked buckets.
    transient Trader emailNext;

    private final String email;

    public Trader(long id, String mnem, String display, String email) {
        super(RecType.TRADER, id, mnem, display);
        this.email = email;
    }

    @Override
    public final String toString() {
        return StringUtil.toJson(this, null);
    }

    @Override
    public final void toJson(Map<String, String> params, Appendable out) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"email\":\"").append(email);
        out.append("\"}");
    }

    public final String getEmail() {
        return email;
    }
}

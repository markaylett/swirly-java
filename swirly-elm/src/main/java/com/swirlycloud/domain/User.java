/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import java.io.IOException;

import com.swirlycloud.util.AshUtil;

public final class User extends Rec {
    // Internals.
    // Singly-linked buckets.
    transient User emailNext;

    private final String email;

    public User(long id, String mnem, String display, String email) {
        super(RecType.USER, id, mnem, display);
        this.email = email;
    }

    @Override
    public final String toString() {
        return AshUtil.toJson(this, null);
    }

    @Override
    public final void toJson(Appendable out, Object arg) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"email\":\"").append(email);
        out.append("\"}");
    }

    public final String getEmail() {
        return email;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.util.Params;
import com.swirlycloud.twirly.util.StringUtil;

public final class Trader extends Rec {
    // Internals.
    // Singly-linked buckets.
    transient Trader emailNext;

    private final String email;

    public Trader(long id, String mnem, String display, String email) {
        super(RecType.TRADER, id, mnem, display);
        this.email = email;
    }

    public static Trader parse(JsonParser p) throws IOException {
        long id = 0;
        String mnem = null;
        String display = null;
        String email = null;

        String key = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Trader(id, mnem, display, email);
            case KEY_NAME:
                key = p.getString();
                break;
            case VALUE_NUMBER:
                if ("id".equals(key)) {
                    id = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", key));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(key)) {
                    mnem = p.getString();
                } else if ("display".equals(key)) {
                    display = p.getString();
                } else if ("email".equals(key)) {
                    email = p.getString();
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", key));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    @Override
    public final String toString() {
        return StringUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out)
            throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"email\":\"").append(email);
        out.append("\"}");
    }

    public final String getEmail() {
        return email;
    }
}

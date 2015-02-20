/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.util.Params;

public final class Trader extends Rec {

    private final String email;

    public Trader(String mnem, String display, String email) {
        super(mnem, display);
        this.email = email;
    }

    public static Trader parse(JsonParser p) throws IOException {
        String mnem = null;
        String display = null;
        String email = null;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Trader(mnem, display, email);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    display = p.getString();
                } else if ("email".equals(name)) {
                    email = p.getString();
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    @Override
    public final void toJson(Params params, Appendable out) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"email\":\"").append(email);
        out.append("\"}");
    }

    @Override
    public final RecType getRecType() {
        return RecType.TRADER;
    }

    public final String getEmail() {
        return email;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.util.JsonUtil.isInternal;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Params;

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

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Trader(id, mnem, display, email);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
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
    public final int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Trader other = (Trader) obj;
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out) throws IOException {
        out.append('{');
        if (isInternal(params)) {
            out.append("\"id\":").append(String.valueOf(id)).append(',');
        }
        out.append("\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"email\":\"").append(email);
        out.append("\"}");
    }

    public final String getEmail() {
        return email;
    }
}

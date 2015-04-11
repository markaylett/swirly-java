/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.domain.Action;

public final class Request {

    public static final int ID = 1 << 0;
    public static final int MNEM = 1 << 1;
    public static final int DISPLAY = 1 << 2;
    public static final int EMAIL = 1 << 3;
    public static final int CONTR = 1 << 4;
    public static final int SETTL_DATE = 1 << 5;
    public static final int EXPIRY_DATE = 1 << 6;
    public static final int STATE = 1 << 7;
    public static final int REF = 1 << 8;
    public static final int ACTION = 1 << 9;
    public static final int TICKS = 1 << 10;
    public static final int LOTS = 1 << 11;
    public static final int MIN_LOTS = 1 << 12;

    private int fields;

    private long id;
    private String mnem;
    private String display;
    private String email;
    private String contr;
    private int settlDate;
    private int expiryDate;
    private int state;
    private String ref;
    private Action action;
    private long ticks;
    private long lots;
    private long minLots;

    public final void clear() {
        fields = 0;
        id = 0;
        mnem = null;
        display = null;
        email = null;
        contr = null;
        settlDate = 0;
        expiryDate = 0;
        state = 0;
        ref = null;
        action = null;
        ticks = 0;
        lots = 0;
        minLots = 0;
    }

    public final void parse(JsonParser p) throws IOException {
        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                break;
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NULL:
                if ("mnem".equals(name)) {
                    fields |= MNEM;
                    mnem = null;
                } else if ("display".equals(name)) {
                    fields |= DISPLAY;
                    display = null;
                } else if ("email".equals(name)) {
                    fields |= EMAIL;
                    email = null;
                } else if ("contr".equals(name)) {
                    fields |= CONTR;
                    contr = null;
                } else if ("ref".equals(name)) {
                    fields |= REF;
                    ref = null;
                } else if ("action".equals(name)) {
                    fields |= ACTION;
                    action = null;
                } else {
                    throw new IOException(String.format("unexpected nullable field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    fields |= ID;
                    id = p.getLong();
                } else if ("settlDate".equals(name)) {
                    fields |= SETTL_DATE;
                    settlDate = p.getInt();
                } else if ("expiryDate".equals(name)) {
                    fields |= EXPIRY_DATE;
                    expiryDate = p.getInt();
                } else if ("state".equals(name)) {
                    fields |= STATE;
                    state = p.getInt();
                } else if ("ticks".equals(name)) {
                    fields |= TICKS;
                    ticks = p.getLong();
                } else if ("lots".equals(name)) {
                    fields |= LOTS;
                    lots = p.getLong();
                } else if ("minLots".equals(name)) {
                    fields |= MIN_LOTS;
                    minLots = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    fields |= MNEM;
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    fields |= DISPLAY;
                    display = p.getString();
                } else if ("email".equals(name)) {
                    fields |= EMAIL;
                    email = p.getString();
                } else if ("contr".equals(name)) {
                    fields |= CONTR;
                    contr = p.getString();
                } else if ("ref".equals(name)) {
                    fields |= REF;
                    ref = p.getString();
                } else if ("action".equals(name)) {
                    fields |= ACTION;
                    action = Action.valueOf(p.getString());
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
    }

    public final int getFields() {
        return fields;
    }

    public final long getId() {
        return id;
    }

    public final String getMnem() {
        return mnem;
    }

    public final String getDisplay() {
        return display;
    }

    public final String getEmail() {
        return email;
    }

    public final String getContr() {
        return contr;
    }

    public final int getSettlDate() {
        return settlDate;
    }

    public final int getExpiryDate() {
        return expiryDate;
    }

    public final int getState() {
        return state;
    }

    public final String getRef() {
        return ref;
    }

    public final Action getAction() {
        return action;
    }

    public final long getTicks() {
        return ticks;
    }

    public final long getLots() {
        return lots;
    }

    public final long getMinLots() {
        return minLots;
    }
}

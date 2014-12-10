/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import java.io.IOException;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

import com.swirlycloud.domain.Action;

public final class Request implements ContentHandler {

    public static final int ID = 1 << 0;
    public static final int MNEM = 1 << 1;
    public static final int DISPLAY = 1 << 2;
    public static final int EMAIL = 1 << 3;
    public static final int CONTR = 1 << 4;
    public static final int SETTL_DATE = 1 << 5;
    public static final int EXPIRY_DATE = 1 << 6;
    public static final int REF = 1 << 7;
    public static final int ACTION = 1 << 8;
    public static final int TICKS = 1 << 9;
    public static final int LOTS = 1 << 10;
    public static final int MIN_LOTS = 1 << 11;

    private transient String key;
    private boolean valid;
    private int fields;

    private long id;
    private String mnem;
    private String display;
    private String email;
    private String contr;
    private int settlDate;
    private int expiryDate;
    private String ref;
    private Action action;
    private long ticks;
    private long lots;
    private long minLots;

    @Override
    public final boolean endArray() throws ParseException, IOException {
        assert false;
        return false;
    }

    @Override
    public final void endJSON() throws ParseException, IOException {
        valid = true;
    }

    @Override
    public final boolean endObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public final boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    @Override
    public final boolean primitive(Object value) throws ParseException, IOException {
        if ("id".equals(key)) {
            if (!(value instanceof Long) || (fields & ID) != 0) {
                return false;
            }
            fields |= ID;
            id = (Long) value;
        } else if ("mnem".equals(key)) {
            if (!(value instanceof String) || (fields & MNEM) != 0) {
                return false;
            }
            fields |= MNEM;
            mnem = (String) value;
        } else if ("display".equals(key)) {
            if (!(value instanceof String) || (fields & DISPLAY) != 0) {
                return false;
            }
            fields |= DISPLAY;
            display = (String) value;
        } else if ("email".equals(key)) {
            if (!(value instanceof String) || (fields & EMAIL) != 0) {
                return false;
            }
            fields |= EMAIL;
            email = (String) value;
        } else if ("contr".equals(key)) {
            if (!(value instanceof String) || (fields & CONTR) != 0) {
                return false;
            }
            fields |= CONTR;
            contr = (String) value;
        } else if ("settlDate".equals(key)) {
            if (!(value instanceof Long) || (fields & SETTL_DATE) != 0) {
                return false;
            }
            fields |= SETTL_DATE;
            settlDate = ((Long) value).intValue();
        } else if ("expiryDate".equals(key)) {
            if (!(value instanceof Long) || (fields & EXPIRY_DATE) != 0) {
                return false;
            }
            fields |= EXPIRY_DATE;
            expiryDate = ((Long) value).intValue();
        } else if ("ref".equals(key)) {
            if (!(value instanceof String) || (fields & REF) != 0) {
                return false;
            }
            fields |= REF;
            ref = (String) value;
        } else if ("action".equals(key)) {
            if (!(value instanceof String) || (fields & ACTION) != 0) {
                return false;
            }
            fields |= ACTION;
            action = Action.valueOf((String) value);
        } else if ("ticks".equals(key)) {
            if (!(value instanceof Long) || (fields & TICKS) != 0) {
                return false;
            }
            fields |= TICKS;
            ticks = (Long) value;
        } else if ("lots".equals(key)) {
            if (!(value instanceof Long) || (fields & LOTS) != 0) {
                return false;
            }
            fields |= LOTS;
            lots = (Long) value;
        } else if ("minLots".equals(key)) {
            if (!(value instanceof Long) || (fields & MIN_LOTS) != 0) {
                return false;
            }
            fields |= MIN_LOTS;
            minLots = (Long) value;
        } else {
            return false;
        }
        return true;
    }

    @Override
    public final boolean startArray() throws ParseException, IOException {
        return false;
    }

    @Override
    public final void startJSON() throws ParseException, IOException {
    }

    @Override
    public final boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public final boolean startObjectEntry(String key) throws ParseException, IOException {
        this.key = key;
        return true;
    }

    public final boolean isValid() {
        return valid;
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

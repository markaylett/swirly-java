/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import java.io.IOException;

import org.doobry.domain.Action;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

public final class Request implements ContentHandler {

    public static final int ID = 1 << 0;
    public static final int CONTR = 1 << 1;
    public static final int SETTL_DATE = 1 << 2;
    public static final int REF = 1 << 3;
    public static final int ACTION = 1 << 4;
    public static final int TICKS = 1 << 5;
    public static final int LOTS = 1 << 6;
    public static final int MIN_LOTS = 1 << 7;

    private transient String key;
    private boolean valid;
    private int fields;
    private long id;
    private String contr;
    private int settlDate;
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
        if (key.equals("id")) {
            if (!(value instanceof Long) || (fields & ID) != 0)
                return false;
            fields |= ID;
            id = (Long) value;
        } else if (key.equals("contr")) {
            if (!(value instanceof String) || (fields & CONTR) != 0)
                return false;
            fields |= CONTR;
            contr = (String) value;
        } else if (key.equals("settlDate")) {
            if (!(value instanceof Long) || (fields & SETTL_DATE) != 0)
                return false;
            fields |= SETTL_DATE;
            settlDate = ((Long) value).intValue();
        } else if (key.equals("ref")) {
            if (!(value instanceof String) || (fields & REF) != 0)
                return false;
            fields |= REF;
            ref = (String) value;
        } else if (key.equals("action")) {
            if (!(value instanceof String) || (fields & ACTION) != 0)
                return false;
            fields |= ACTION;
            action = Action.valueOf((String) value);
        } else if (key.equals("ticks")) {
            if (!(value instanceof Long) || (fields & TICKS) != 0)
                return false;
            fields |= TICKS;
            ticks = (Long) value;
        } else if (key.equals("lots")) {
            if (!(value instanceof Long) || (fields & LOTS) != 0)
                return false;
            fields |= LOTS;
            lots = (Long) value;
        } else if (key.equals("minLots")) {
            if (!(value instanceof Long) || (fields & MIN_LOTS) != 0)
                return false;
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

    public final String getContr() {
        return contr;
    }

    public final int getSettlDate() {
        return settlDate;
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

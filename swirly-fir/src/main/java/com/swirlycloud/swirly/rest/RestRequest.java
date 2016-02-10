/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.swirly.domain.Role;
import com.swirlycloud.swirly.domain.Side;

public final class RestRequest {

    public static final int MNEM = 1 << 0;
    public static final int DISPLAY = 1 << 1;
    public static final int EMAIL = 1 << 2;
    public static final int TRADER = 1 << 3;
    public static final int CONTR = 1 << 4;
    public static final int SETTL_DATE = 1 << 5;
    public static final int EXPIRY_DATE = 1 << 6;
    public static final int REF = 1 << 7;
    public static final int QUOTE_ID = 1 << 8;
    public static final int STATE = 1 << 9;
    public static final int SIDE = 1 << 10;
    public static final int LOTS = 1 << 11;
    public static final int TICKS = 1 << 12;
    public static final int MIN_LOTS = 1 << 13;
    public static final int ROLE = 1 << 14;
    public static final int CPTY = 1 << 15;

    private int fields;

    private String mnem;
    private String display;
    private String email;
    private String trader;
    private String contr;
    private int settlDate;
    private int expiryDate;
    private String ref;
    private long quoteId;
    private int state;
    private Side side;
    private long lots;
    private long ticks;
    private long minLots;
    private Role role;
    private String cpty;

    public final void clear() {
        fields = 0;
        mnem = null;
        display = null;
        email = null;
        trader = null;
        contr = null;
        settlDate = 0;
        expiryDate = 0;
        ref = null;
        quoteId = 0;
        state = 0;
        side = null;
        lots = 0;
        ticks = 0;
        minLots = 0;
        role = null;
        cpty = null;
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
                    fields &= ~MNEM;
                    mnem = null;
                } else if ("display".equals(name)) {
                    fields &= ~DISPLAY;
                    display = null;
                } else if ("email".equals(name)) {
                    fields &= ~EMAIL;
                    email = null;
                } else if ("trader".equals(name)) {
                    fields &= ~TRADER;
                    trader = null;
                } else if ("contr".equals(name)) {
                    fields &= ~CONTR;
                    contr = null;
                } else if ("settlDate".equals(name)) {
                    fields &= ~SETTL_DATE;
                    settlDate = 0;
                } else if ("expiryDate".equals(name)) {
                    fields &= ~EXPIRY_DATE;
                    expiryDate = 0;
                } else if ("ref".equals(name)) {
                    fields &= ~REF;
                    ref = null;
                } else if ("side".equals(name)) {
                    fields &= ~SIDE;
                    side = null;
                } else if ("role".equals(name)) {
                    fields &= ~ROLE;
                    role = null;
                } else if ("cpty".equals(name)) {
                    fields &= ~CPTY;
                    cpty = null;
                } else {
                    throw new IOException(String.format("unexpected nullable field '%s'", name));
                }
                break;
            case VALUE_NUMBER:
                if ("settlDate".equals(name)) {
                    fields |= SETTL_DATE;
                    settlDate = p.getInt();
                } else if ("expiryDate".equals(name)) {
                    fields |= EXPIRY_DATE;
                    expiryDate = p.getInt();
                } else if ("quoteId".equals(name)) {
                    fields |= QUOTE_ID;
                    quoteId = p.getLong();
                } else if ("state".equals(name)) {
                    fields |= STATE;
                    state = p.getInt();
                } else if ("lots".equals(name)) {
                    fields |= LOTS;
                    lots = p.getLong();
                } else if ("ticks".equals(name)) {
                    fields |= TICKS;
                    ticks = p.getLong();
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
                } else if ("trader".equals(name)) {
                    fields |= TRADER;
                    trader = p.getString();
                } else if ("contr".equals(name)) {
                    fields |= CONTR;
                    contr = p.getString();
                } else if ("ref".equals(name)) {
                    fields |= REF;
                    ref = p.getString();
                } else if ("side".equals(name)) {
                    fields |= SIDE;
                    final String s = p.getString();
                    assert s != null;
                    side = Side.valueOf(s);
                } else if ("role".equals(name)) {
                    fields |= ROLE;
                    role = Role.valueOf(p.getString());
                } else if ("cpty".equals(name)) {
                    fields |= CPTY;
                    cpty = p.getString();
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

    public final boolean isMnemSet() {
        return (fields & MNEM) != 0;
    }

    public final String getMnem() {
        return mnem;
    }

    public final boolean isDisplaySet() {
        return (fields & DISPLAY) != 0;
    }

    public final String getDisplay() {
        return display;
    }

    public final boolean isEmailSet() {
        return (fields & EMAIL) != 0;
    }

    public final String getEmail() {
        return email;
    }

    public final boolean isTraderSet() {
        return (fields & TRADER) != 0;
    }

    public final String getTrader() {
        return trader;
    }

    public final boolean isContrSet() {
        return (fields & CONTR) != 0;
    }

    public final String getContr() {
        return contr;
    }

    public final boolean isSettlDateSet() {
        return (fields & SETTL_DATE) != 0;
    }

    public final int getSettlDate() {
        return settlDate;
    }

    public final boolean isExpiryDateSet() {
        return (fields & EXPIRY_DATE) != 0;
    }

    public final int getExpiryDate() {
        return expiryDate;
    }

    public final boolean isRefSet() {
        return (fields & REF) != 0;
    }

    public final String getRef() {
        return ref;
    }

    public final boolean isQuoteIdSet() {
        return (fields & QUOTE_ID) != 0;
    }

    public final long getQuoteId() {
        return quoteId;
    }

    public final boolean isStateSet() {
        return (fields & STATE) != 0;
    }

    public final int getState() {
        return state;
    }

    public final boolean isSideSet() {
        return (fields & SIDE) != 0;
    }

    public final Side getSide() {
        return side;
    }

    public final boolean isLotsSet() {
        return (fields & LOTS) != 0;
    }

    public final long getLots() {
        return lots;
    }

    public final boolean isTicksSet() {
        return (fields & TICKS) != 0;
    }

    public final long getTicks() {
        return ticks;
    }

    public final boolean isMinLotsSet() {
        return (fields & MIN_LOTS) != 0;
    }

    public final long getMinLots() {
        return minLots;
    }

    public final boolean isRoleSet() {
        return (fields & ROLE) != 0;
    }

    public final Role getRole() {
        return role;
    }

    public final boolean isCptySet() {
        return (fields & CPTY) != 0;
    }

    public final String getCpty() {
        return cpty;
    }

    /**
     * Validate fields.
     * 
     * @param required
     *            Required fields.
     * @return true if fields are value.
     */
    public final boolean isValid(int required) {
        return (fields & required) == required;
    }

    /**
     * Validate fields.
     * 
     * @param required
     *            Required fields.
     * @param optional
     *            Optional fields.
     * @return true if fields are value.
     */
    public final boolean isValid(int required, int optional) {
        return (fields & required) == required //
                && (fields & ~(required | optional)) == 0;
    }
}

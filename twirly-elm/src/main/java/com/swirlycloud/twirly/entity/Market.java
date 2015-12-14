/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;
import static com.swirlycloud.twirly.util.MnemUtil.newMnem;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Params;

/**
 * A place where buyers and sellers come together to exchange goods or services.
 * 
 * @author Mark Aylett
 */
public @NonNullByDefault class Market extends AbstractRec implements Financial {

    private static final long serialVersionUID = 1L;

    protected Memorable contr;
    protected final int settlDay;
    protected final int expiryDay;
    protected int state;

    protected Market(String mnem, @Nullable String display, Memorable contr, int settlDay,
            int expiryDay, int state) {
        super(mnem, display);
        assert (settlDay == 0) == (expiryDay == 0);
        this.contr = contr;
        this.settlDay = settlDay;
        this.expiryDay = expiryDay;
        this.state = state;
    }

    public static Market parse(JsonParser p) throws IOException {
        String mnem = null;
        String display = null;
        Memorable contr = null;
        int settlDay = 0;
        int expiryDay = 0;
        int state = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                if (mnem == null) {
                    throw new IOException("mnem is null");
                }
                if (contr == null) {
                    throw new IOException("contr is null");
                }
                return new Market(mnem, display, contr, settlDay, expiryDay, state);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NULL:
                if ("settlDate".equals(name)) {
                    settlDay = 0;
                } else if ("expiryDate".equals(name)) {
                    expiryDay = 0;
                }
                break;
            case VALUE_NUMBER:
                if ("settlDate".equals(name)) {
                    settlDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("expiryDate".equals(name)) {
                    expiryDay = JulianDay.maybeIsoToJd(p.getInt());
                } else if ("state".equals(name)) {
                    state = p.getInt();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    display = p.getString();
                } else if ("contr".equals(name)) {
                    final String s = p.getString();
                    assert s != null;
                    contr = newMnem(s);
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
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"contr\":\"").append(contr.getMnem());
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
        out.append(",\"expiryDate\":");
        if (expiryDay != 0) {
            out.append(String.valueOf(jdToIso(expiryDay)));
        } else {
            out.append("null");
        }
        out.append(",\"state\":").append(String.valueOf(state));
        out.append('}');
    }

    public final void enrich(Contr contr) {
        assert this.contr.getMnem().equals(contr.getMnem());
        this.contr = contr;
    }

    public final void setState(int state) {
        this.state = state;
    }

    @Override
    public final RecType getRecType() {
        return RecType.MARKET;
    }

    @Override
    public final String getMarket() {
        return mnem;
    }

    @Override
    public final String getContr() {
        return contr.getMnem();
    }

    @Deprecated
    public final Contr getContrRich() {
        return (Contr) contr;
    }

    @Override
    public final int getSettlDay() {
        return settlDay;
    }

    @Override
    public final boolean isSettlDaySet() {
        return settlDay != 0;
    }

    /**
     * @return the market expiry-day.
     */
    public final int getExpiryDay() {
        return expiryDay;
    }

    public final boolean isExpiryDaySet() {
        return expiryDay != 0;
    }

    public final int getState() {
        return state;
    }
}

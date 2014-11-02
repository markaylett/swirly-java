package org.doobry.web;

import java.io.IOException;

import org.doobry.domain.Action;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class Rest implements ContentHandler {

    public static final int ID = 1 << 0;
    public static final int ACCNT = 1 << 1;
    public static final int GIVEUP = 1 << 2;
    public static final int CONTR = 1 << 3;
    public static final int SETTL_DATE = 1 << 4;
    public static final int REF = 1 << 5;
    public static final int ACTION = 1 << 6;
    public static final int TICKS = 1 << 7;
    public static final int LOTS = 1 << 8;
    public static final int MIN_LOTS = 1 << 9;

    private transient String key;
    private boolean end;
    private int fields;
    private long id;
    private String accnt;
    private String giveup;
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
        end = true;
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
        System.out.println(key + "=" + value.getClass().getCanonicalName());
        if (key.equals("id")) {
            if (!(value instanceof Long) || (fields & ID) != 0)
                return false;
            fields |= ID;
            id = (Long) value;
        } else if (key.equals("accnt")) {
            if (!(value instanceof String) || (fields & ACCNT) != 0)
                return false;
            fields |= ACCNT;
            accnt = (String) value;
        } else if (key.equals("giveup")) {
            if (!(value instanceof String) || (fields & GIVEUP) != 0)
                return false;
            fields |= GIVEUP;
            giveup = (String) value;
        } else if (key.equals("contr")) {
            if (!(value instanceof String) || (fields & CONTR) != 0)
                return false;
            fields |= CONTR;
            contr = (String) value;
        } else if (key.equals("settl_date")) {
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
        } else if (key.equals("min_lots")) {
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

    public final boolean isEnd() {
        return end;
    }

    public final int getFields() {
        return fields;
    }

    public final long getId() {
        return id;
    }

    public final String getAccnt() {
        return accnt;
    }

    public final String getGiveup() {
        return giveup;
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

    public static void main(String[] args) throws ParseException {
        final JSONParser p = new JSONParser();
        final Rest r = new Rest();
        p.parse("{\"giveup\":101}", r);
        System.out.println(r.isEnd());
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.exception.UncheckedIOException;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static final Params INTERNAL = new Params() {
        @SuppressWarnings("unchecked")
        @Override
        public final <T> T getParam(String name, Class<T> clazz) {
            return "internal".equals(name) ? (T) Boolean.TRUE : null;
        }
    };

    public static String toJson(Jsonifiable j) {
        final StringBuilder sb = new StringBuilder();
        try {
            j.toJson(INTERNAL, sb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }

    public static String getIdOrMnem(Identifiable iden, Params params) {
        return params != null && params.getParam("internal", Boolean.class) == Boolean.TRUE //
        ? String.valueOf(iden.getId()) //
                : '"' + ((Memorable) iden).getMnem() + '"';
    }

    public static void parseStartObject(JsonParser p) throws IOException {
        if (!p.hasNext()) {
            throw new IOException("start object not found");
        }
        final Event event = p.next();
        if (event != Event.START_OBJECT) {
            throw new IOException(String.format("unexpected json token '%s'", event));
        }
    }
}

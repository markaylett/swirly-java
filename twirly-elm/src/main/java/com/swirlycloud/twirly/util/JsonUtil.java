/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.exception.UncheckedIOException;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static Params withExpired(final Params params) {
        return new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "expired".equals(name) //
                ? (T) Boolean.TRUE
                        : params.getParam(name, clazz);
            }
        };
    }

    public static final @NonNull Params PARAMS_NONE = new Params() {
        @Override
        public final <T> T getParam(String name, Class<T> clazz) {
            return null;
        }
    };

    public static final @NonNull Params PARAMS_EXPIRED = new Params() {
        @SuppressWarnings("unchecked")
        @Override
        public final <T> T getParam(String name, Class<T> clazz) {
            return "expired".equals(name) ? (T) Boolean.TRUE : null;
        }
    };

    @SuppressWarnings("null")
    public static @NonNull String toJson(Jsonifiable j) {
        final StringBuilder sb = new StringBuilder();
        try {
            j.toJson(PARAMS_NONE, sb);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }

    public static void toJsonArray(SlNode node, Params params, Appendable out) throws IOException {
        out.append('[');
        for (int i = 0; node != null; node = node.slNext()) {
            final Jsonifiable j = (Jsonifiable) node;
            if (i > 0) {
                out.append(',');
            }
            j.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    public static void toJsonArray(RbNode node, Params params, Appendable out) throws IOException {
        out.append('[');
        for (int i = 0; node != null; node = node.rbNext()) {
            final Jsonifiable j = (Jsonifiable) node;
            if (i > 0) {
                out.append(',');
            }
            j.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    public static void parseStartArray(JsonParser p) throws IOException {
        if (!p.hasNext()) {
            throw new IOException("start array not found");
        }
        final Event event = p.next();
        if (event != Event.START_ARRAY) {
            throw new IOException(String.format("unexpected json token '%s'", event));
        }
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

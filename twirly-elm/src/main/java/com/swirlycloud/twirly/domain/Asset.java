/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Params;

public final class Asset extends Rec {
    private final AssetType type;

    public Asset(long id, String mnem, String display, AssetType type) {
        super(RecType.ASSET, id, mnem, display);
        this.type = type;
    }

    public static Asset parse(JsonParser p) throws IOException {
        long id = 0;
        String mnem = null;
        String display = null;
        AssetType type = null;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Asset(id, mnem, display, type);
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
                } else if ("type".equals(name)) {
                    type = AssetType.valueOf(p.getString());
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
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out)
            throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"type\":\"").append(type.name());
        out.append("\"}");
    }

    public final AssetType getAssetType() {
        return type;
    }
}

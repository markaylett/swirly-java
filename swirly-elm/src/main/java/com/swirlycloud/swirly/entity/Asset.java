/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.util.Params;

/**
 * An item of value.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class Asset extends AbstractRec {

    private static final long serialVersionUID = 1L;
    private final AssetType type;

    protected Asset(String mnem, @Nullable String display, AssetType type) {
        super(mnem, display);
        this.type = type;
    }

    public static Asset parse(JsonParser p) throws IOException {
        String mnem = null;
        String display = null;
        AssetType type = null;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                if (mnem == null) {
                    throw new IOException("mnem is null");
                }
                if (type == null) {
                    throw new IOException("type is null");
                }
                return new Asset(mnem, display, type);
            case KEY_NAME:
                name = p.getString();
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
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"type\":\"").append(type.name());
        out.append("\"}");
    }

    @Override
    public final RecType getRecType() {
        return RecType.ASSET;
    }

    public final AssetType getAssetType() {
        return type;
    }
}

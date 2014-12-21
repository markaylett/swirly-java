/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import java.io.IOException;

import com.swirlycloud.twirly.function.UnaryFunction;
import com.swirlycloud.twirly.util.StringUtil;

public final class Asset extends Rec {
    private final AssetType type;

    public Asset(long id, String mnem, String display, AssetType type) {
        super(RecType.ASSET, id, mnem, display);
        this.type = type;
    }

    @Override
    public final String toString() {
        return StringUtil.toJson(this, null);
    }

    @Override
    public final void toJson(UnaryFunction<String, String> params, Appendable out)
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

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public final class Asset extends Rec {
    private final AssetType type;

    public Asset(long id, String mnem, String display, AssetType type) {
        super(RecType.ASSET, id, mnem, display);
        this.type = type;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        print(sb, null);
        return sb.toString();
    }

    @Override
    public final void print(StringBuilder sb, Object arg) {
        sb.append("{\"mnem\":\"").append(mnem);
        sb.append("\",\"display\":\"").append(display);
        sb.append("\",\"type\":\"").append(type);
        sb.append("\"}");
    }

    public final AssetType getAssetType() {
        return type;
    }

}

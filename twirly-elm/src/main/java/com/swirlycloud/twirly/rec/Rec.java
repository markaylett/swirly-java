/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Memorable;

public abstract @NonNullByDefault class Rec extends BasicRbNode implements Jsonifiable, Memorable {

    private static final long serialVersionUID = 1L;

    protected final String mnem;
    protected String display;

    public Rec(String mnem, @Nullable String display) {
        this.mnem = mnem;
        this.display = display != null ? display : mnem;
    }

    @Override
    public final int hashCode() {
        return mnem.hashCode();
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rec other = (Rec) obj;
        if (!mnem.equals(other.mnem)) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    public void setDisplay(@Nullable String display) {
        this.display = display != null ? display : mnem;
    }

    public abstract RecType getRecType();

    @Override
    public final String getMnem() {
        return mnem;
    }

    public final String getDisplay() {
        return display;
    }
}

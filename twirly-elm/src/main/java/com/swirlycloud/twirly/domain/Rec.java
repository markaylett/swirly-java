/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Memorable;

public abstract @NonNullByDefault class Rec extends BasicRbNode implements Cloneable,
        Jsonifiable, SlNode, Memorable {

    private static final long serialVersionUID = 1L;

    private transient @Nullable SlNode slNext;

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

    @Override
    public final Rec clone() {
        try {
            final Rec rec = (Rec) super.clone();
            // Nullify intrusive nodes.
            rec.slNext = null;
            return rec;
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void setSlNext(@Nullable SlNode next) {
        this.slNext = next;
    }

    @Override
    public final @Nullable SlNode slNext() {
        return slNext;
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

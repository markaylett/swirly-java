/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Memorable;

public abstract class Rec extends BasicRbNode implements Cloneable, Jsonifiable, SlNode, Memorable {

    private transient SlNode next;

    protected final String mnem;
    protected String display;

    public Rec(String mnem, String display) {
        assert mnem != null;
        this.mnem = mnem;
        this.display = display != null ? display : mnem;
    }

    @Override
    public final int hashCode() {
        return mnem.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
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
        Rec rec = null;
        try {
            rec = (Rec) super.clone();
            // Nullify intrusive nodes.
            rec.next = null;
        } catch (final CloneNotSupportedException e) {
            assert false;
        }
        return rec;
    }

    @Override
    public final void setSlNext(SlNode next) {
        this.next = next;
    }

    @Override
    public final SlNode slNext() {
        return next;
    }

    public void setDisplay(String display) {
        this.display = display;
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

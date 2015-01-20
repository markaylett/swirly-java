/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.intrusive.BasicSlNode;
import com.swirlycloud.twirly.util.Identifiable;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Jsonifiable;

public abstract class Rec extends BasicSlNode implements Identifiable, Memorable, Jsonifiable {
    private transient Rec idNext;
    private transient Rec mnemNext;
    protected final RecType recType;
    protected final long id;
    protected final String mnem;
    protected final String display;

    public Rec(RecType recType, long id, String mnem, String display) {
        this.recType = recType;
        this.id = id;
        this.mnem = mnem;
        this.display = display;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((recType == null) ? 0 : recType.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((mnem == null) ? 0 : mnem.hashCode());
        result = prime * result + ((display == null) ? 0 : display.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
        if (recType != other.recType) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (mnem == null) {
            if (other.mnem != null) {
                return false;
            }
        } else if (!mnem.equals(other.mnem)) {
            return false;
        }
        if (display == null) {
            if (other.display != null) {
                return false;
            }
        } else if (!display.equals(other.display)) {
            return false;
        }
        return true;
    }

    public final void setIdNext(Rec idNext) {
        this.idNext = idNext;
    }

    public final Rec idNext() {
        return idNext;
    }

    public final void setMnemNext(Rec mnemNext) {
        this.mnemNext = mnemNext;
    }

    public final Rec mnemNext() {
        return mnemNext;
    }

    public final RecType getRecType() {
        return recType;
    }

    @Override
    public final long getId() {
        return id;
    }

    @Override
    public final String getMnem() {
        return mnem;
    }

    public final String getDisplay() {
        return display;
    }
}

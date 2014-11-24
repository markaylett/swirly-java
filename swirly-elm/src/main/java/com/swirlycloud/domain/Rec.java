/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import com.swirlycloud.util.BasicSlNode;
import com.swirlycloud.util.Identifiable;
import com.swirlycloud.util.Memorable;
import com.swirlycloud.util.Printable;

public abstract class Rec extends BasicSlNode implements Identifiable, Memorable, Printable {
    private transient Rec idNext;
    private transient Rec mnemNext;
    protected final Kind kind;
    protected final long id;
    protected final String mnem;
    protected final String display;

    public Rec(Kind kind, long id, String mnem, String display) {
        this.kind = kind;
        this.id = id;
        this.mnem = mnem;
        this.display = display;
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

    public final Kind getKind() {
        return kind;
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
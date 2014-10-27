/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import org.doobry.util.BasicSlNode;
import org.doobry.util.Identifiable;
import org.doobry.util.Memorable;

public abstract class Rec extends BasicSlNode implements Identifiable, Memorable {
    private transient Rec idNext;
    private transient Rec mnemNext;
    private final RecType type;
    private final long id;
    private final String mnem;
    private final String display;

    public Rec(RecType type, long id, String mnem, String display) {
        this.type = type;
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

    public final RecType getType() {
        return type;
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

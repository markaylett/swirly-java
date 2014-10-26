/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public final class Party extends Rec {
    private final String email;
    private transient Object accnt;

    public Party(long id, String mnem, String display, String email) {
        super(RecType.PARTY, id, mnem, display);
        this.email = email;
    }

    public final void setAccnt(Object accnt) {
        this.accnt = accnt;
    }

    public final String getEmail() {
        return email;
    }

    public final Object getAccnt() {
        return accnt;
    }
}

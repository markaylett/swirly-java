/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public final class Perm {
    private final Party trader;
    private final Party giveup;

    public Perm(Party trader, Party giveup) {
        this.trader = trader;
        this.giveup = giveup;
    }

    public final Party getTrader() {
        return trader;
    }

    public final Party getGiveup() {
        return giveup;
    }
}

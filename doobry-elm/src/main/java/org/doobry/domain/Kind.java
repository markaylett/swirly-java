/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public enum Kind {
    /**
     * Asset.
     */
    ASSET(1),
    /**
     * Contract.
     */
    CONTR(2),
    /**
     * User.
     */
    USER(3),
    /**
     * Order.
     */
    ORDER(4),
    /**
     * Execution.
     */
    EXEC(5);
    private final int value;

    private Kind(int value) {
        this.value = value;
    }

    public final int intValue() {
        return this.value;
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public enum Reg {
    /**
     * Order identifier register index.
     */
    ORDER_ID(0),
    /**
     * Match identifier register index.
     */
    MATCH_ID(1),
    /**
     * Execution identifier register index.
     */
    EXEC_ID(2);
    private final int value;

    private Reg(int value) {
        this.value = value;
    }

    public final int intValue() {
        return this.value;
    }
}

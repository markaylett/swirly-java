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
    ASSET(1, "Asset"),
    /**
     * Contract.
     */
    CONTR(2, "Contr"),
    /**
     * User.
     */
    USER(3, "User"),
    /**
     * Order.
     */
    ORDER(4, "Order"),
    /**
     * Execution.
     */
    EXEC(5, "Exec");
    private final int value;
    private final String camelName;

    private Kind(int value, String camelName) {
        this.value = value;
        this.camelName = camelName;
    }

    public final int intValue() {
        return this.value;
    }

    public final String camelName() {
        return camelName;
    }
}
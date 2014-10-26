/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

public final class AshFactory {
    private AshFactory() {
    }
    public static Identifiable newId(final long id) {
        return new Identifiable() {
            @Override
            public final long getId() {
                return id;
            }
        };
    }
    public static final Identifiable ZERO_ID = newId(0);
}

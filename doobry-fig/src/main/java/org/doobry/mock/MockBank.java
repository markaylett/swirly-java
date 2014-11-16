/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import java.util.Arrays;

import org.doobry.domain.Kind;
import org.doobry.engine.Bank;

public final class MockBank implements Bank {
    private final long[] arr;

    public MockBank() {
        arr = new long[Kind.values().length];
        Arrays.fill(arr, 1);
    }

    @Override
    public final long allocIds(Kind kind, long val) {
        final long nextId = arr[kind.ordinal()]; 
        arr[kind.ordinal()] += val;
        return nextId;
    }
}

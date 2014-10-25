/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import org.doobry.util.Bank;

public final class MockBank implements Bank {
    private final long[] arr;

    public MockBank(int n) {
        arr = new long[n];
    }

    @Override
    public final long load(int reg) {
        return arr[reg];
    }

    @Override
    public final void store(int reg, long val) {
        arr[reg] = val;
    }

    @Override
    public final long addFetch(int reg, long val) {
        return ++arr[reg];
    }
}

/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

/**
 * A bank of registers (not a financial institution.)
 */
public interface Bank {

    long load(int reg);

    void store(int reg, long val);

    long addFetch(int reg, long val);
}

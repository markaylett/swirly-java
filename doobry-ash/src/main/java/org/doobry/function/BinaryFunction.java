/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.function;

public interface BinaryFunction<R, T, U> {
    R call(T lhs, U rhs);
}

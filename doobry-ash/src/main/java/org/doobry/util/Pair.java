/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

import javax.annotation.concurrent.Immutable;

@Immutable
public interface Pair<T, U> {
    T getFirst();

    U getSecond();
}

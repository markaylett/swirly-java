/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import javax.annotation.concurrent.Immutable;

@Immutable
public interface Pair<T, U> {
    T getFirst();

    U getSecond();
}
